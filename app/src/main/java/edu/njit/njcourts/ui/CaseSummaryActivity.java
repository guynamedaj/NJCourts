package edu.njit.njcourts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.njit.njcourts.R;
import edu.njit.njcourts.adapters.EvidenceAdapter;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.utils.NetworkUtils;

/**
 * Task 27 & 30: Evidence Dashboard + Mobile Data Warning.
 */
public class CaseSummaryActivity extends AppCompatActivity {

    private String ticketId;
    private EvidenceAdapter adapter;
    private AppDatabase db;
    private TextView textVehicleInfo, textViolationInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_summary);

        ticketId = getIntent().getStringExtra("TICKET_ID");
        db = AppDatabase.getDatabase(this);

        textVehicleInfo = findViewById(R.id.text_vehicle_info);
        textViolationInfo = findViewById(R.id.text_violation_info);

        // Fetch Ticket details to show in Review screen
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

        // Observe database for this ticket's photos
        db.evidenceDao().getEvidenceForTicket(ticketId).observe(this, evidence -> {
            if (evidence != null) {
                adapter.setEvidenceList(evidence);
            }
        });

        ImageButton btnBack = findViewById(R.id.btn_back_to_selection);
        btnBack.setOnClickListener(v -> finish());

        Button btnTakePhoto = findViewById(R.id.btn_add_more);
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraCaptureActivity.class);
            intent.putExtra("TICKET_ID", ticketId);
            startActivity(intent);
        });

        // Task 30: Implement Sync with Mobile Data Warning
        Button btnSync = findViewById(R.id.btn_sync);
        btnSync.setOnClickListener(v -> handleSyncAttempt());
    }

    private void handleSyncAttempt() {
        NetworkUtils.NetworkType type = NetworkUtils.getNetworkType(this);

        if (type == NetworkUtils.NetworkType.NONE) {
            Toast.makeText(this, "No internet connection. Please connect and try again.", Toast.LENGTH_LONG).show();
        } else if (type == NetworkUtils.NetworkType.MOBILE_DATA) {
            showMobileDataWarning();
        } else {
            // On WiFi - Proceed to sync (Task 31 will implement actual upload)
            startSyncProcess();
        }
    }

    private void showMobileDataWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Mobile Data Warning")
                .setMessage("You are currently using mobile data. Syncing photos may incur additional charges from your carrier. Would you like to proceed or wait for Wi-Fi?")
                .setPositiveButton("Sync Now", (dialog, which) -> startSyncProcess())
                .setNegativeButton("Wait for Wi-Fi", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void startSyncProcess() {
        // Task 31 (Retrofit Upload) is scheduled for Sprint 4 (post-midterm).
        // For the midterm demo, we display a "Work In Progress" notice.
        new AlertDialog.Builder(this)
                .setTitle("Cloud Sync: Work In Progress")
                .setMessage("Local evidence validation and storage are complete for the Midterm Milestone.\n\nThe Cloud Upload (Task 31) is scheduled for Sprint 4 development.\n\nThank you for viewing our POC!")
                .setPositiveButton("Got it", null)
                .show();
    }
}
