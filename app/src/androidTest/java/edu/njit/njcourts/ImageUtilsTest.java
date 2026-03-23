package edu.njit.njcourts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.Assert.*;

import edu.njit.njcourts.utils.ImageUtils;

@RunWith(AndroidJUnit4.class)
public class ImageUtilsTest {

    @Test
    public void testStressCompressRealWorldScenario() {
        // 1. Create a massive 12MP-style image (4000x3000)
        // This would normally be ~36MB in raw memory (ARGB_8888)
        Bitmap hugeBitmap = Bitmap.createBitmap(4000, 3000, Bitmap.Config.ARGB_8888);
        
        // 2. Fill it with high-entropy data (noise) to simulate a complex real-world scene
        // JPEG compression struggles with noise, so this is a true stress test.
        Canvas canvas = new Canvas(hugeBitmap);
        Random random = new Random();
        Paint paint = new Paint();
        
        for (int i = 0; i < 1000; i++) {
            paint.setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            canvas.drawRect(random.nextInt(4000), random.nextInt(3000), 
                           random.nextInt(4000), random.nextInt(3000), paint);
        }

        // 3. Compress it using our utility
        long startTime = System.currentTimeMillis();
        byte[] compressedData = ImageUtils.compressForDatabase(hugeBitmap);
        long endTime = System.currentTimeMillis();
        
        int sizeInKB = compressedData.length / 1024;
        System.out.println("--- Compression Stress Test ---");
        System.out.println("Original Resolution: 4000x3000");
        System.out.println("Compressed Size: " + sizeInKB + " KB");
        System.out.println("Time taken: " + (endTime - startTime) + "ms");

        // 4. Assertions
        assertNotNull("Compressed data should not be null", compressedData);
        assertTrue("Compressed size (" + sizeInKB + "KB) MUST be under 250KB", compressedData.length <= 250 * 1024);
        
        // 5. Verify the image is still valid/readable
        Bitmap decoded = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
        assertNotNull("Compressed data must be a valid readable image", decoded);
        assertTrue("Image should have been resized to Full HD limits", 
                   decoded.getWidth() <= 1920 && decoded.getHeight() <= 1920);

        // Cleanup
        hugeBitmap.recycle();
        decoded.recycle();
    }
}
