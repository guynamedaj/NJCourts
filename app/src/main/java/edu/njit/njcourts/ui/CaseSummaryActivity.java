package edu.njit.njcourts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import edu.njit.njcourts.R;
import edu.njit.njcourts.adapters.EvidenceAdapter;
import edu.njit.njcourts.adapters.EvidenceItem;
import edu.njit.njcourts.data.ApiEvidence;
import edu.njit.njcourts.data.ApiService;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.PhotoEvidenceEntity;
import edu.njit.njcourts.data.RetrofitClient;
import edu.njit.njcourts.utils.NetworkUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Task 27 + 31: Evidence dashboard with inline submit.
 *
 * Displays the union of:
 *   - local Room photos for the ticket (LOCAL_ONLY + SYNCED/FAILED)
 *   - remote photos fetched from the backend for the ticket
 *
 * De-duped by (fileName, timestamp) so a synced photo doesn't appear twice.
 * Submit uploads every non-SYNCED local photo and refreshes the remote list.
 */
public class CaseSummaryActivity extends AppCompatActivity {

    private static final String TAG = "CaseSummaryActivity";
    private static final String STATUS_SYNCED = "SYNCED";
    private static final String STATUS_FAILED = "FAILED";

    private String ticketId;
    private EvidenceAdapter adapter;
    private AppDatabase db;
    private TextView textVehicleInfo, textViolationInfo;
    private Button btnSubmit;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private List<PhotoEvidenceEntity> latestLocal = new ArrayList<>();
    private List<ApiEvidence> latestRemote = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_summary);

        ticketId = getIntent().getStringExtra("TICKET_ID");
        db = AppDatabase.getDatabase(this);

        textVehicleInfo = findViewById(R.id.text_vehicle_info);
        textViolationInfo = findViewById(R.id.text_violation_info);

        db.ticketDao().getTicketById(ticketId).observe(this, ticket -> {
            if (ticket != null) {
                textVehicleInfo.setText(ticket.vehicleSummary);
                textViolationInfo.setText(ticket.violation);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_evidence);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new EvidenceAdapter();
        recyclerView.setAdapter(adapter);

        db.evidenceDao().getEvidenceForTicket(ticketId).observe(this, evidence -> {
            latestLocal = evidence != null ? evidence : new ArrayList<>();
            rebuildList();
        });

        fetchRemoteEvidence();

        ImageButton btnBack = findViewById(R.id.btn_back_to_selection);
        btnBack.setOnClickListener(v -> finish());

        Button btnTakePhoto = findViewById(R.id.btn_add_more);
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraCaptureActivity.class);
            intent.putExtra("TICKET_ID", ticketId);
            startActivity(intent);
        });

        btnSubmit = findViewById(R.id.btn_review_submit);
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRemoteEvidence();
    }

    private void fetchRemoteEvidence() {
        RetrofitClient.get().getTicketEvidence(ticketId).enqueue(new Callback<List<ApiEvidence>>() {
            @Override
            public void onResponse(Call<List<ApiEvidence>> call, Response<List<ApiEvidence>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "getTicketEvidence HTTP " + response.code());
                    return;
                }
                latestRemote = response.body();
                rebuildList();
            }

            @Override
            public void onFailure(Call<List<ApiEvidence>> call, Throwable t) {
                Log.e(TAG, "getTicketEvidence failed", t);
            }
        });
    }

    private void rebuildList() {
        List<EvidenceItem> merged = new ArrayList<>();
        Set<String> localKeys = new HashSet<>();

        for (PhotoEvidenceEntity e : latestLocal) {
            merged.add(EvidenceItem.fromLocal(e));
            localKeys.add(dedupKey(e.originalFilename, e.timestamp));
        }
        for (ApiEvidence r : latestRemote) {
            long ts = 0L;
            try {
                ts = Instant.parse(r.uploadTimestamp).toEpochMilli();
            } catch (Exception ignored) {}
            if (localKeys.contains(dedupKey(r.fileName, ts))) continue;
            merged.add(EvidenceItem.fromRemote(r));
        }
        Collections.sort(merged, Comparator.comparingLong((EvidenceItem it) -> it.timestampMs).reversed());
        adapter.setItems(merged);
    }

    private static String dedupKey(String fileName, long timestampMs) {
        return (fileName == null ? "" : fileName) + "|" + timestampMs;
    }

    private void handleSubmit() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "Take at least one photo before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }

        NetworkUtils.NetworkType type = NetworkUtils.getNetworkType(this);
        if (type == NetworkUtils.NetworkType.NONE) {
            Toast.makeText(this, "No internet connection. Connect and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        if (type == NetworkUtils.NetworkType.MOBILE_DATA) {
            new AlertDialog.Builder(this)
                .setTitle("Mobile Data Warning")
                .setMessage("Submitting on mobile data may incur carrier charges. Continue?")
                .setPositiveButton("Submit", (d, w) -> runUpload())
                .setNegativeButton("Wait for Wi-Fi", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
            return;
        }
        runUpload();
    }

    private void runUpload() {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        ioExecutor.execute(() -> {
            List<PhotoEvidenceEntity> unsynced = db.evidenceDao().getUnsyncedEvidenceSync(ticketId);
            if (unsynced.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Nothing to submit", Toast.LENGTH_SHORT).show();
                    resetSubmitButton();
                });
                return;
            }

            final int total = unsynced.size();
            final AtomicInteger completed = new AtomicInteger(0);
            final AtomicInteger failed = new AtomicInteger(0);
            final ApiService api = RetrofitClient.get();

            for (PhotoEvidenceEntity photo : unsynced) {
                uploadOne(api, photo, total, completed, failed);
            }
        });
    }

    private void uploadOne(
        ApiService api,
        PhotoEvidenceEntity photo,
        int total,
        AtomicInteger completed,
        AtomicInteger failed
    ) {
        String isoTimestamp = Instant.ofEpochMilli(photo.timestamp).toString();

        MediaType textType = MediaType.parse("text/plain");
        MediaType imageType = MediaType.parse("image/jpeg");
        RequestBody ticketBody = RequestBody.create(photo.ticketNumber, textType);
        RequestBody tsBody = RequestBody.create(isoTimestamp, textType);
        RequestBody photoBody = RequestBody.create(photo.photoBlob, imageType);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData(
            "photo", photo.originalFilename, photoBody
        );

        Call<ResponseBody> call = api.uploadPhoto(ticketBody, tsBody, photoPart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    markStatus(photo.id, STATUS_SYNCED);
                    completed.incrementAndGet();
                } else {
                    Log.w(TAG, "Upload failed for photo " + photo.id + ": HTTP " + response.code());
                    markStatus(photo.id, STATUS_FAILED);
                    failed.incrementAndGet();
                }
                maybeReportDone(total, completed, failed);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Upload error for photo " + photo.id, t);
                markStatus(photo.id, STATUS_FAILED);
                failed.incrementAndGet();
                maybeReportDone(total, completed, failed);
            }
        });
    }

    private void markStatus(int photoId, String status) {
        ioExecutor.execute(() -> db.evidenceDao().updateSyncStatus(photoId, status));
    }

    private void maybeReportDone(int total, AtomicInteger completed, AtomicInteger failed) {
        int done = completed.get() + failed.get();
        if (done < total) return;

        final int ok = completed.get();
        final int bad = failed.get();
        runOnUiThread(() -> {
            String msg;
            if (bad == 0) {
                msg = "Submitted " + ok + " photo(s)";
            } else if (ok == 0) {
                msg = "Submit failed for all " + total + " photo(s)";
            } else {
                msg = "Submitted " + ok + ", " + bad + " failed";
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            resetSubmitButton();
            fetchRemoteEvidence();
        });
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit Ticket \u2192");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
