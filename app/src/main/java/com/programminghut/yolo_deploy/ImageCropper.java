package com.programminghut.yolo_deploy;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

public class ImageCropper {

    public static Bitmap cropImage(Canvas canvas, RectF boundingBox) {
        // Create a Bitmap from the Canvas
        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        // Calculate the cropping coordinates
        int left = (int) boundingBox.left;
        int top = (int) boundingBox.top;
        int right = (int) boundingBox.right;
        int bottom = (int) boundingBox.bottom;

        // Make sure the cropping coordinates are within bounds
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(bitmap.getWidth(), right);
        bottom = Math.min(bitmap.getHeight(), bottom);

        // Create a cropped Bitmap
        int width = right - left;
        int height = bottom - top;
        Bitmap croppedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Apply the cropping using Matrix
        Matrix matrix = new Matrix();
        matrix.postTranslate(-left, -top);
        tempCanvas.drawBitmap(bitmap, matrix, null);

        // Return the cropped Bitmap
        return croppedBitmap;
    }
}
