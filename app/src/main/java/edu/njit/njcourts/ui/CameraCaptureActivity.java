package edu.njit.njcourts.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.List;

import edu.njit.njcourts.R;

public class CameraCaptureActivity extends AppCompatActivity {

    private static final String TAG = "CameraCapture";
    
    private PreviewView previewView;
    private Button btnBack, btnCapture, btnTestSaved;
    private ImageCapture imageCapture;
    
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

        previewView = findViewById(R.id.previewView);
        btnBack = findViewById(R.id.btn_back);
        btnCapture = findViewById(R.id.btn_capture);
        btnTestSaved = findViewById(R.id.btn_test_saved);

        btnBack.setOnClickListener(v -> finish());
        btnCapture.setOnClickListener(v -> takePhoto());
        btnTestSaved.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        initializeDetectors();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initializeDetectors() {
        // 1. Object Detector (Generic)
        ObjectDetectorOptions objOptions = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();
        objectDetector = ObjectDetection.getClient(objOptions);

        // 2. Face Detector (Specific for identifying people)
        FaceDetectorOptions faceOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        faceDetector = FaceDetection.getClient(faceOptions);
        
        Log.d(TAG, "Detectors initialized");
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
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            if (bitmap == null) return;

            bitmap = correctBitmapRotation(imageUri, bitmap);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            // Run both detectors in parallel
            Task<List<DetectedObject>> objTask = objectDetector.process(image);
            Task<List<Face>> faceTask = faceDetector.process(image);

            Tasks.whenAllComplete(objTask, faceTask).addOnCompleteListener(t -> {
                btnCapture.setEnabled(true);
                btnTestSaved.setEnabled(true);

                boolean personOrFaceFound = false;
                StringBuilder debugLabels = new StringBuilder("Found: ");

                // Check Face results
                if (faceTask.isSuccessful() && !faceTask.getResult().isEmpty()) {
                    personOrFaceFound = true;
                    debugLabels.append("[Face] ");
                }

                // Check Object results
                if (objTask.isSuccessful()) {
                    for (DetectedObject obj : objTask.getResult()) {
                        for (DetectedObject.Label label : obj.getLabels()) {
                            String text = label.getText();
                            debugLabels.append(text).append(" ");
                            if ("People".equalsIgnoreCase(text) || "Fashion good".equalsIgnoreCase(text)) {
                                personOrFaceFound = true;
                            }
                        }
                    }
                }

                Toast.makeText(this, debugLabels.toString(), Toast.LENGTH_SHORT).show();

                if (personOrFaceFound) {
                    Toast.makeText(this, " Person/Face detected. Please retake.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, " Photo accepted.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Processing error", e);
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
}
