package edu.njit.njcourts.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import java.util.concurrent.Executors;

import edu.njit.njcourts.R;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.PhotoEvidenceEntity;
import edu.njit.njcourts.utils.ImageUtils;

/**
 * Task 18, 20 & 21: Camera Integration + ML Validation + Strictness Toggle.
 */
public class CameraCaptureActivity extends AppCompatActivity {

    private static final String TAG = "CameraCapture";
    
    private PreviewView previewView;
    private View btnCapture; // Changed to View to support FrameLayout shutter
    private ImageButton btnTestSaved;
    private ImageCapture imageCapture;

    private View containerPreview;
    private ImageView imgCompressedPreview;
    private TextView textCompressionInfo;
    private TextView textOriginalInfo;
    private MaterialButton btnRetake;
    private MaterialButton btnSave;
    private View btnToggleCompare;
    private View layoutCameraControls;
    private View overlayLoading;
    
    // Task 21: Strictness Toggle & Info
    private SwitchMaterial switchStrictMode;
    private ImageButton btnStrictnessInfo;
    private View cardStrictness;

    private String ticketId;
    private byte[] latestCompressedData;
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
                    detectWithLoading(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        ticketId = getIntent().getStringExtra("TICKET_ID");
        if (ticketId == null) {
            finish();
            return;
        }

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
        overlayLoading = findViewById(R.id.overlay_loading);
        switchStrictMode = findViewById(R.id.switch_strict_mode);
        btnStrictnessInfo = findViewById(R.id.btn_strictness_info);
        cardStrictness = findViewById(R.id.card_strictness);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnCapture.setOnClickListener(v -> takePhoto());
        btnTestSaved.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnRetake.setOnClickListener(v -> {
            containerPreview.setVisibility(View.GONE);
            layoutCameraControls.setVisibility(View.VISIBLE);
            cardStrictness.setVisibility(View.VISIBLE);
            clearBitmaps();
        });
        btnSave.setOnClickListener(v -> saveToDatabase());
        btnToggleCompare.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (originalBitmap != null) {
                    imgCompressedPreview.setImageBitmap(originalBitmap);
                    textCompressionInfo.setText("VIEWING: ORIGINAL IMAGE");
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (compressedBitmap != null) {
                    imgCompressedPreview.setImageBitmap(compressedBitmap);
                    textCompressionInfo.setText("VIEWING: COMPRESSED VERSION");
                }
                return true;
            }
            return false;
        });
        btnStrictnessInfo.setOnClickListener(v -> showStrictnessInfoDialog());
    }

    private void showStrictnessInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Validation Modes")
                .setMessage("BALANCED (OFF):\nRejects image only if a face is clearly visible. Recommended for busy areas.\n\nSTRICT (ON):\nRejects image if any human element (faces, poses, body shapes) is detected. Ensures maximum evidence purity.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void saveToDatabase() {
        if (latestCompressedData == null) return;
        btnSave.setEnabled(false);
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            PhotoEvidenceEntity evidence = new PhotoEvidenceEntity(
                    ticketId, "img_" + System.currentTimeMillis() + ".jpg",
                    System.currentTimeMillis(), "PASSED", "LOCAL_ONLY", latestCompressedData);
            db.evidenceDao().insertEvidence(evidence);
            runOnUiThread(() -> finish());
        });
    }

    private void initializeDetectors() {
        faceDetector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build());
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f).build());
        poseDetector = PoseDetection.getClient(new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE).build());
        selfieSegmenter = Segmentation.getClient(new SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) { Log.e(TAG, "Camera init failed", e); }
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
                        detectWithLoading(Uri.fromFile(photoFile));
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        btnCapture.setEnabled(true);
                        btnTestSaved.setEnabled(true);
                    }
                });
    }

    private void detectWithLoading(Uri imageUri) {
        overlayLoading.setVisibility(View.VISIBLE);
        containerPreview.setVisibility(View.VISIBLE);
        layoutCameraControls.setVisibility(View.GONE);
        cardStrictness.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        detectPersonWithMLKit(imageUri);
    }

    private void detectPersonWithMLKit(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            long originalSizeBytes = 0;
            try (InputStream countStream = getContentResolver().openInputStream(imageUri)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = countStream.read(buffer)) != -1) originalSizeBytes += read;
            }
            final String originalSizeText = (originalSizeBytes / 1024) + " KB";
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            if (bitmap == null) { overlayLoading.setVisibility(View.GONE); return; }
            bitmap = correctBitmapRotation(imageUri, bitmap);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            Task<List<Face>> faceTask = faceDetector.process(image);
            Task<List<ImageLabel>> labelTask = imageLabeler.process(image);
            Task<Pose> poseTask = poseDetector.process(image);
            Task<SegmentationMask> segmentTask = selfieSegmenter.process(image);

            final Bitmap finalBitmap = bitmap;
            Tasks.whenAllComplete(faceTask, labelTask, poseTask, segmentTask).addOnCompleteListener(t -> {
                overlayLoading.setVisibility(View.GONE);
                btnCapture.setEnabled(true);
                btnTestSaved.setEnabled(true);

                boolean isStrict = switchStrictMode.isChecked();
                boolean faceDetected = faceTask.isSuccessful() && !faceTask.getResult().isEmpty();
                boolean poseDetected = poseTask.isSuccessful() && !poseTask.getResult().getAllPoseLandmarks().isEmpty();
                
                boolean segmentationHit = false;
                if (segmentTask.isSuccessful() && segmentTask.getResult() != null) {
                    SegmentationMask mask = segmentTask.getResult();
                    ByteBuffer buffer = mask.getBuffer();
                    float totalPixels = mask.getWidth() * mask.getHeight();
                    float personPixels = 0;
                    for (int i = 0; i < totalPixels; i++) if (buffer.getFloat() > 0.4) personPixels++;
                    if ((personPixels / totalPixels) > 0.1) segmentationHit = true;
                }

                boolean labelHit = false;
                if (labelTask.isSuccessful() && labelTask.getResult() != null) {
                    for (ImageLabel label : labelTask.getResult()) {
                        String txt = label.getText().toLowerCase();
                        if (txt.contains("person") || txt.contains("human")) labelHit = true;
                    }
                }

                boolean reject = faceDetected; 
                String reason = "A face was detected.";

                if (isStrict) { 
                    if (poseDetected) { reject = true; reason = "A human pose was detected."; }
                    if (segmentationHit) { reject = true; reason = "Significant human-shaped area detected."; }
                    if (labelHit) { reject = true; reason = "Image labeling identified a person."; }
                }

                if (reject) {
                    showValidationError(reason);
                } else {
                    processValidImage(finalBitmap, originalSizeText);
                }
            });
        } catch (IOException e) { overlayLoading.setVisibility(View.GONE); }
    }

    private void showValidationError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Validation Failed")
                .setMessage(message + "\nPlease retake the photo of the vehicle only.")
                .setPositiveButton("RETAKE", (dialog, which) -> {
                    containerPreview.setVisibility(View.GONE);
                    layoutCameraControls.setVisibility(View.VISIBLE);
                    cardStrictness.setVisibility(View.VISIBLE);
                    clearBitmaps();
                })
                .setCancelable(false).show();
    }

    private void processValidImage(Bitmap bitmap, String originalSizeText) {
        clearBitmaps();
        this.originalBitmap = bitmap;
        this.latestCompressedData = ImageUtils.compressForDatabase(bitmap);
        this.compressedBitmap = BitmapFactory.decodeByteArray(latestCompressedData, 0, latestCompressedData.length);
        
        textOriginalInfo.setText("Original: " + originalSizeText + " | Compressed: " + (latestCompressedData.length / 1024) + " KB");
        imgCompressedPreview.setImageBitmap(compressedBitmap);
        btnSave.setEnabled(true);
        Toast.makeText(this, "Validation Successful!", Toast.LENGTH_LONG).show();
    }

    private void clearBitmaps() {
        latestCompressedData = null;
        if (originalBitmap != null) originalBitmap.recycle();
        if (compressedBitmap != null) compressedBitmap.recycle();
        originalBitmap = null;
        compressedBitmap = null;
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
        } catch (Exception e) { return bitmap; }
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
