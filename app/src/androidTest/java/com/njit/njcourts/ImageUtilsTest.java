package com.njit.njcourts;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import edu.njit.njcourts.utils.ImageUtils;

/**
 * Instrumented test for ImageUtils compression logic.
 * Uses Android's Bitmap class, so it must run on a device or emulator.
 */
@RunWith(AndroidJUnit4.class)
public class ImageUtilsTest {

    @Test
    public void testCompressToUnder250KB() {
        // Create a large dummy bitmap (4000x3000 = ~12MP)
        // This simulates a high-res camera photo.
        Bitmap largeBitmap = Bitmap.createBitmap(4000, 3000, Bitmap.Config.ARGB_8888);
        
        // Fill it with some random colors to make it harder to compress
        largeBitmap.eraseColor(Color.BLUE); 
        
        // Compress it
        byte[] compressedData = ImageUtils.compressForDatabase(largeBitmap);
        
        // Log the final size for verification
        int sizeInKB = compressedData.length / 1024;
        System.out.println("Compressed image size: " + sizeInKB + " KB");
        
        // Assertions
        assertNotNull("Compressed data should not be null", compressedData);
        assertTrue("Compressed size (" + sizeInKB + "KB) should be under 250KB", compressedData.length <= 250 * 1024);
        assertTrue("Compressed data should contain something", compressedData.length > 0);
        
        // Clean up
        largeBitmap.recycle();
    }
}
