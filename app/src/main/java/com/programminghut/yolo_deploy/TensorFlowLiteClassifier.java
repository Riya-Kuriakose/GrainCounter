package com.programminghut.yolo_deploy;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TensorFlowLiteClassifier {

    private static final String TAG = "TensorFlowLiteClassifier";

    private static final int IMAGE_SIZE = 50;
    private static final int NUM_CLASSES=2;

    private Interpreter tflite;
    private Context context;

    public TensorFlowLiteClassifier(Context context, String modelPath) throws IOException {
        this.context = context;
        this.tflite = new Interpreter(loadModelFile(modelPath));
    }

    public String classifyImage(Bitmap image) {
        try {
            // Preprocess image
            Bitmap scaledImage = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false);
            ByteBuffer byteBuffer = preprocessImage(scaledImage);

            // Perform inference
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, IMAGE_SIZE, IMAGE_SIZE, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);
            //System.out.println("qwertyu " + inputFeature0);
            float[][] output = new float[1][NUM_CLASSES];
            tflite.run(inputFeature0.getBuffer(), output);
//            for (int i = 0; i < output.length; i++) {
//                for (int j = 0; j < output[i].length; j++) {
//                    System.out.println("Output[" + i + "][" + j + "]: " + output[i][j]);
//                }
//            }

            //System.out.println("classi results:"+output);// Process output
            return processOutput(output);
        } catch (Exception e) {
            Log.e(TAG, "Error classifying image: " + e.getMessage());
            return null;
        }
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer preprocessImage(Bitmap image) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE; i++) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
            }
        }
        return byteBuffer;
    }

    private String processOutput(float[][] output) {
        for (int i = 0; i < output.length; i++) {
            if (output[i][0] >  output[i][1])
            { return "Full";
            }
            else {
                return "Broken";
            }
            }
        return "Unknown";
        }


    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
}
