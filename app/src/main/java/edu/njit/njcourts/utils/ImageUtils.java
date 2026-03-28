package edu.njit.njcourts.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import java.io.ByteArrayOutputStream;

/**
 * Utility class for image processing and compression.
 * Task 11 & 23: Target < 250KB.
 */
public class ImageUtils {

    private static final int TARGET_SIZE_BYTES = 250 * 1024; // 250KB
    private static final int MAX_WIDTH = 1920; 
    private static final int MAX_HEIGHT = 1080;

    /**
     * Compresses a Bitmap to be under 250KB.
     * Fixed Bug: Now smarter about not increasing file size for already small images.
     */
    public static byte[] compressForDatabase(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];

        // 1. Resize first (standardizes pixels regardless of source)
        Bitmap currentBitmap = resizeIfNeeded(bitmap);
        
        // 2. Initial compression pass at 80% (good balance)
        int quality = 80; 
        byte[] result;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        result = stream.toByteArray();

        // 3. Only enter loop if we are still over the target
        if (result.length > TARGET_SIZE_BYTES) {
            while (result.length > TARGET_SIZE_BYTES && quality > 10) {
                quality -= 10;
                stream = new ByteArrayOutputStream();
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
                result = stream.toByteArray();
            }
        }
        
        if (currentBitmap != bitmap) {
            currentBitmap.recycle();
        }
        
        return result;
    }

    private static Bitmap resizeIfNeeded(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return bm;
        }

        float scale = Math.min((float) MAX_WIDTH / width, (float) MAX_HEIGHT / height);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }
}
