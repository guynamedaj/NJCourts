package edu.njit.njcourts.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import java.io.ByteArrayOutputStream;

/**
 * Utility class for image processing and compression.
 */
public class ImageUtils {

    private static final int TARGET_SIZE_BYTES = 250 * 1024; // 250KB
    private static final int MAX_WIDTH = 1920; // Full HD
    private static final int MAX_HEIGHT = 1080;

    /**
     * Compresses a Bitmap to be under 250KB while preserving as much quality as possible.
     * This method first resizes the image if it exceeds HD resolution, then iteratively
     * adjusts JPEG quality until the target size is met.
     *
     * @param bitmap The source Bitmap to compress.
     * @return A byte array containing the compressed image data.
     */
    public static byte[] compressForDatabase(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];

        // 1. Resize if necessary to avoid processing massive images
        Bitmap currentBitmap = resizeIfNeeded(bitmap);
        
        // 2. Iterative compression
        int quality = 90;
        byte[] result;
        ByteArrayOutputStream stream;
        
        do {
            stream = new ByteArrayOutputStream();
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            result = stream.toByteArray();
            
            // Log for debugging (optional in production)
            // android.util.Log.d("ImageUtils", "Quality: " + quality + ", Size: " + result.length / 1024 + "KB");
            
            quality -= 10;
        } while (result.length > TARGET_SIZE_BYTES && quality > 10);
        
        // Cleanup if we created a new bitmap during resize
        if (currentBitmap != bitmap) {
            currentBitmap.recycle();
        }
        
        return result;
    }

    /**
     * Resizes the bitmap to fit within MAX_WIDTH and MAX_HEIGHT while maintaining aspect ratio.
     */
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
