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
import android.widget.ImageView;
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
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

import edu.njit.njcourts.R;
import edu.njit.njcourts.utils.ImageUtils;

public class CameraCaptureActivity extends AppCompatActivity {

    private static final String TAG = "CameraCapture";
    
    private PreviewView previewView;
    private Button btnCapture;
    private Button btnTestSaved;
    private ImageCapture imageCapture;

    private View containerPreview;
    private ImageView imgCompressedPreview;
    private TextView textCompressionInfo;
    private TextView textOriginalInfo;
    private Button btnRetake;
    private Button btnSave;
    private Button btnToggleCompare;
    private View layoutCameraControls;

    // Bitmaps for Comparison
    private Bitmap originalBitmap;
    private Bitmap compressedBitmap;

    private FaceDetector faceDetector;
    private ImageLabeler imageLabeler;
    private PoseDetector poseDetector;
    private Segmenter selfieSegmenter;

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
        Button btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        if (btnCapture != null) {
            btnCapture.setOnClickListener(v -> takePhoto());
        }
        
        if (btnTestSaved != null) {
            btnTestSaved.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }
        
        if (btnRetake != null) {
            btnRetake.setOnClickListener(v -> {
                containerPreview.setVisibility(View.GONE);
                layoutCameraControls.setVisibility(View.VISIBLE);
                clearBitmaps();
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(this, "Photo accepted and saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        // Toggle Comparison: Hold to see original, Release to see compressed
        if (btnToggleCompare != null) {
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
    }

    private void initializeDetectors() {
        // 1. Face Detector
        faceDetector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build());

        // 2. Image Labeler
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build());

        // 3. Pose Detector
        poseDetector = PoseDetection.getClient(new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build());

        // 4. Selfie Segmenter (Detects human outline)
        selfieSegmenter = Segmentation.getClient(new SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build());

        Log.d(TAG, "All detectors initialized");
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

            // Run all detectors in parallel
            Task<List<Face>> faceTask = faceDetector.process(image);
            Task<List<ImageLabel>> labelTask = imageLabeler.process(image);
            Task<Pose> poseTask = poseDetector.process(image);
            Task<SegmentationMask> segmentTask = selfieSegmenter.process(image);

            final Bitmap finalBitmap = bitmap;

            Tasks.whenAllComplete(faceTask, labelTask, poseTask, segmentTask).addOnCompleteListener(t -> {
                btnCapture.setEnabled(true);
                btnTestSaved.setEnabled(true);

                boolean personFound = false;
                StringBuilder debugLabels = new StringBuilder("Found: ");

                // 1. Check Face
                if (faceTask.isSuccessful() && faceTask.getResult() != null && !faceTask.getResult().isEmpty()) {
                    personFound = true;
                    debugLabels.append("[Face] ");
                }

                // 2. Check Pose
                if (poseTask.isSuccessful() && poseTask.getResult() != null) {
                    if (!poseTask.getResult().getAllPoseLandmarks().isEmpty()) {
                        personFound = true;
                        debugLabels.append("[Pose] ");
                    }
                }

                // 3. Check Segmentation (Human Outline)
                if (segmentTask.isSuccessful() && segmentTask.getResult() != null) {
                    SegmentationMask mask = segmentTask.getResult();
                    ByteBuffer buffer = mask.getBuffer();
                    int width = mask.getWidth();
                    int height = mask.getHeight();

                    float totalPersonPixels = 0;
                    for (int i = 0; i < width * height; i++) {
                        if (buffer.getFloat() > 0.4) totalPersonPixels++;
                    }
                    float personPercentage = (totalPersonPixels / (width * height)) * 100;
                    if (personPercentage > 10.0) { // If human covers > 10% of the image
                        personFound = true;
                        debugLabels.append(String.format(Locale.US, "[Seg:%.1f%%] ", personPercentage));
                    }
                }

                // 4. Check Labels
                if (labelTask.isSuccessful() && labelTask.getResult() != null) {
                    for (ImageLabel label : labelTask.getResult()) {
                        String text = label.getText();
                        debugLabels.append(String.format(Locale.US, "%s (%.2f) ", text, label.getConfidence()));
                        if (text.toLowerCase().contains("person") || text.toLowerCase().contains("human") ||
                            text.toLowerCase().contains("man") || text.toLowerCase().contains("woman")) {
                            personFound = true;
                        }
                    }
                }

                if (personFound) {
                    Toast.makeText(this, "Person detected. Please retake photo of vehicle only.", Toast.LENGTH_LONG).show();
                } else {
                    clearBitmaps();
                    this.originalBitmap = finalBitmap;
                    
                    // Compress the image iteratively to under 250KB
                    byte[] compressedData = ImageUtils.compressForDatabase(finalBitmap);
                    this.compressedBitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
                    
                    showComparisonUI(originalSizeText, (compressedData.length / 1024) + " KB");
                    Toast.makeText(this, "Photo accepted and compressed.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Processing error", e);
        }
    }

    private void showComparisonUI(String originalSize, String compressedSize) {
        if (imgCompressedPreview != null) {
            imgCompressedPreview.setImageBitmap(compressedBitmap);
        }
        if (textCompressionInfo != null) {
            textCompressionInfo.setText("VIEWING: COMPRESSED VERSION");
            textCompressionInfo.setTextColor(0xFFFFFFFF);
        }
        if (textOriginalInfo != null) {
            textOriginalInfo.setText("Original Size: " + originalSize + " | Compressed: " + compressedSize);
        }
        
        if (containerPreview != null) containerPreview.setVisibility(View.VISIBLE);
        if (layoutCameraControls != null) layoutCameraControls.setVisibility(View.GONE);
    }

    private void clearBitmaps() {
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        if (compressedBitmap != null && !compressedBitmap.isRecycled()) {
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
        clearBitmaps();
        if (faceDetector != null) faceDetector.close();
        if (imageLabeler != null) imageLabeler.close();
        if (poseDetector != null) poseDetector.close();
        if (selfieSegmenter != null) selfieSegmenter.close();
    }
}
