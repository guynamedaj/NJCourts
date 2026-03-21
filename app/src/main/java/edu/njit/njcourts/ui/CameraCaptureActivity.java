package edu.njit.njcourts.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.DetectedObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.njit.njcourts.R;
import edu.njit.njcourts.utils.ImageUtils;

public class CameraCaptureActivity extends AppCompatActivity {

    private static final String TAG = "CameraCapture";
    
    private PreviewView previewView;
    private Button btnBack, btnCapture, btnTestSaved;
    private ImageCapture imageCapture;
    
    // Preview Overlay Views
    private FrameLayout containerPreview;
    private ImageView imgCompressedPreview;
    private TextView textCompressionInfo;
    private TextView textOriginalInfo;
    private Button btnRetake, btnSave, btnToggleCompare;
    private LinearLayout layoutCameraControls;

    // Bitmaps for Comparison
    private Bitmap originalBitmap;
    private Bitmap compressedBitmap;

    private ObjectDetector objectDetector;
    private FaceDetector faceDetector;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    detectPersonWithMLKit(uri);
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        initializeViews();
        setupClickListeners();
        initializeDetectors();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        btnBack = findViewById(R.id.btn_back);
        btnCapture = findViewById(R.id.btn_capture);
        btnTestSaved = findViewById(R.id.btn_test_saved);
        
        containerPreview = findViewById(R.id.container_preview);
        imgCompressedPreview = findViewById(R.id.img_compressed_preview);
        textCompressionInfo = findViewById(R.id.text_compression_info);
        textOriginalInfo = findViewById(R.id.text_original_info);
        btnRetake = findViewById(R.id.btn_retake);
        btnSave = findViewById(R.id.btn_save);
        btnToggleCompare = findViewById(R.id.btn_toggle_compare);
        layoutCameraControls = findViewById(R.id.layout_camera_controls);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCapture.setOnClickListener(v -> takePhoto());
        btnTestSaved.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        
        btnRetake.setOnClickListener(v -> {
            containerPreview.setVisibility(View.GONE);
            layoutCameraControls.setVisibility(View.VISIBLE);
            clearBitmaps();
        });
        
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Photo saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Toggle Comparison: Hold to see original, Release to see compressed
        btnToggleCompare.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (originalBitmap != null) {
                    imgCompressedPreview.setImageBitmap(originalBitmap);
                    textCompressionInfo.setText("VIEWING: ORIGINAL IMAGE");
                    textCompressionInfo.setTextColor(0xFF00E676); // Green
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (compressedBitmap != null) {
                    imgCompressedPreview.setImageBitmap(compressedBitmap);
                    textCompressionInfo.setText("VIEWING: COMPRESSED VERSION");
                    textCompressionInfo.setTextColor(0xFFFFFFFF); // White
                }
                return true;
            }
            return false;
        });
    }

    private void initializeDetectors() {
        ObjectDetectorOptions objOptions = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();
        objectDetector = ObjectDetection.getClient(objOptions);

        FaceDetectorOptions faceOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        faceDetector = FaceDetection.getClient(faceOptions);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "Camera init failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        btnCapture.setEnabled(false);
        btnTestSaved.setEnabled(false);

        File photoFile = new File(getCacheDir(), "photo_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        detectPersonWithMLKit(Uri.fromFile(photoFile));
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        btnCapture.setEnabled(true);
                        btnTestSaved.setEnabled(true);
                    }
                });
    }

    private void detectPersonWithMLKit(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            
            // Calculate original file size for display
            long originalSizeBytes = 0;
            try (InputStream countStream = getContentResolver().openInputStream(imageUri)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = countStream.read(buffer)) != -1) {
                    originalSizeBytes += read;
                }
            }
            final String originalSizeText = (originalSizeBytes / 1024) + " KB";

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            if (bitmap == null) return;

            bitmap = correctBitmapRotation(imageUri, bitmap);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            Task<List<DetectedObject>> objTask = objectDetector.process(image);
            Task<List<Face>> faceTask = faceDetector.process(image);

            final Bitmap finalBitmap = bitmap; 

            Tasks.whenAllComplete(objTask, faceTask).addOnCompleteListener(t -> {
                btnCapture.setEnabled(true);
                btnTestSaved.setEnabled(true);

                boolean personFound = false;
                if (faceTask.isSuccessful() && !faceTask.getResult().isEmpty()) personFound = true;
                if (objTask.isSuccessful()) {
                    for (DetectedObject obj : objTask.getResult()) {
                        for (DetectedObject.Label label : obj.getLabels()) {
                            if ("People".equalsIgnoreCase(label.getText())) personFound = true;
                        }
                    }
                }

                if (personFound) {
                    Toast.makeText(this, "Person detected. Please retake.", Toast.LENGTH_LONG).show();
                } else {
                    clearBitmaps(); // Clear old ones if any
                    this.originalBitmap = finalBitmap;
                    byte[] compressedData = ImageUtils.compressForDatabase(finalBitmap);
                    this.compressedBitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
                    
                    showComparisonUI(originalSizeText, compressedData.length / 1024 + " KB");
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Processing error", e);
        }
    }

    private void showComparisonUI(String originalSize, String compressedSize) {
        imgCompressedPreview.setImageBitmap(compressedBitmap);
        textCompressionInfo.setText("VIEWING: COMPRESSED VERSION");
        textCompressionInfo.setTextColor(0xFFFFFFFF);
        textOriginalInfo.setText("Original Size: " + originalSize + " | Compressed: " + compressedSize);
        
        containerPreview.setVisibility(View.VISIBLE);
        layoutCameraControls.setVisibility(View.GONE);
    }

    private void clearBitmaps() {
        if (originalBitmap != null) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        if (compressedBitmap != null) {
            compressedBitmap.recycle();
            compressedBitmap = null;
        }
    }

    private Bitmap correctBitmapRotation(Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return bitmap;
            ExifInterface exif = new ExifInterface(inputStream);
            inputStream.close();
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
                case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
                case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
                default: return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return bitmap;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearBitmaps(); // Release memory immediately
        if (objectDetector != null) objectDetector.close();
        if (faceDetector != null) faceDetector.close();
    }
}
