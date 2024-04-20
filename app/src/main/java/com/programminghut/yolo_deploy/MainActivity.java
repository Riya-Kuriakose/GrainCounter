package com.programminghut.yolo_deploy;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int IMAGE_PICK = 100;
    TensorFlowLiteClassifier classifier;
    ImageView imageView;
    Bitmap bitmap;
    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPain = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //System.out.println("hello riya");
        imageView = findViewById(R.id.imageView);

        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("best-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this);

        boxPaint.setStrokeWidth(5);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        textPain.setTextSize(50);
        textPain.setColor(Color.GREEN);
        textPain.setStyle(Paint.Style.FILL);
    }

    public void selectImage(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);
    }

    public void predict(View view){
        ArrayList<Recognition> recognitions =  yolov5TFLiteDetector.detect(bitmap);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap useBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Canvas crop_canvas = new Canvas(mutableBitmap);



        //Bitmap croppedBitmap = ImageCropper.cropImage(canvas, boundingBox);
        //String filePath ="C:\\Users\\riyak\\FL\\yolo_android_working_app\\app\\src\\img.jpg";

        try {
            classifier = new TensorFlowLiteClassifier(this, "model_classi.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Bitmap croppedBitmap;
        //C:\Users\riyak\FL\yolo_android_working_app\app\src\main\assets
        int broken_cnt = 0;
        int full_cnt = 0;
        int total_cnt = 0;
        for(Recognition recognition: recognitions){
            if(recognition.getConfidence() > 0.4){
                total_cnt++;
                RectF location = recognition.getLocation();
                //System.out.println("location"+ location);
                Bitmap croppedBitmap = extractCroppedRegionFromImage(location,useBitmap);
                //croppedBitmap = ImageCropper.cropImage(crop_canvas, location);
                //System.out.println("croppped bitmap:"+croppedBitmap);
                String result = classifier.classifyImage(croppedBitmap);
                System.out.println("croppped bitmap:"+result);
                if (result.equals("Full")) {
                    full_cnt++;
                } else {
                    broken_cnt++;
                }
                canvas.drawRect(location, boxPaint);
                canvas.drawText(recognition.getLabelName() + ":" + recognition.getConfidence(), location.left, location.top, textPain);
            }
        }
        System.out.println("Report");
        System.out.println("Total detected grains: " + total_cnt);
        System.out.println("Full Grains Count: "+full_cnt);
        System.out.println("Broken Grains Count: "+broken_cnt);
        imageView.setImageBitmap(mutableBitmap);
        TextView editTextContent = findViewById(R.id.brokenCount);

        // Set the text of the EditText to the value of brokenGrainCount
        editTextContent.setText(String.valueOf(broken_cnt));
        TextView editTextContent2 = findViewById(R.id.totalCount);

        // Set the text of the EditText to the value of brokenGrainCount
        editTextContent2.setText(String.valueOf(total_cnt));
        TextView editTextContent3 = findViewById(R.id.fullCount);

        // Set the text of the EditText to the value of brokenGrainCount
        editTextContent3.setText(String.valueOf(full_cnt));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_PICK && data != null){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private Bitmap extractCroppedRegionFromImage(RectF location,Bitmap originalBitmap) {
        // Assuming you have access to the original image bitmap
        //Bitmap originalBitmap = ...;

        // Calculate the cropped region
        int left = (int) location.left;
        int top = (int) location.top;
        int right = (int) location.right;
        int bottom = (int) location.bottom;

        // Create a new bitmap for the cropped region
        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top);

        return croppedBitmap;
    }


}