package edu.njit.njcourts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import edu.njit.njcourts.R;
import edu.njit.njcourts.adapters.EvidenceAdapter;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.TicketEntity;
import edu.njit.njcourts.models.Ticket;
import edu.njit.njcourts.utils.NetworkUtils;

public class TicketSelectionActivity extends AppCompatActivity {

    private Spinner spinnerTickets;
    private View sectionTicketDetails;
    private Button btnAttachPhoto;
    private Button btnSync;
    private List<Ticket> demoTickets;
    private Ticket selectedTicket;

    // Inline detail TextViews
    private TextView textLicPlate, textState, textMake, textBodyType, textColor;
    private TextView textViolation, textViolDate, textViolTime, textStreet;
    private TextView textCourtDate, textCourtTime, textCourtCode;

    // Evidence
    private RecyclerView recyclerEvidence;
    private TextView textNoPhotos;
    private EvidenceAdapter evidenceAdapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_selection);

        db = AppDatabase.getDatabase(this);

        initializeViews();
        setupDemoData();
        setupSpinner();
        syncDemoTicketsToDatabaseIfEmpty();
        setupEvidenceRecycler();

        btnAttachPhoto.setOnClickListener(v -> showAttachPhotoOptions());

        btnSync.setOnClickListener(v -> handleSyncAttempt());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh evidence when returning from camera
        if (selectedTicket != null) {
            observeEvidence(selectedTicket.getTicketNumber());
        }
    }

    private void syncDemoTicketsToDatabaseIfEmpty() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = db.ticketDao().getTicketCountSync();
            if (count == 0) {
                List<TicketEntity> entities = new ArrayList<>();
                for (Ticket t : demoTickets) {
                    entities.add(new TicketEntity(t.getTicketNumber(), t.getViolation(),
                        t.getColor() + " " + t.getMake(), "SYNCED"));
                }
                db.ticketDao().insertTickets(entities);
            }
        });
    }

    private void initializeViews() {
        spinnerTickets = findViewById(R.id.spinner_tickets);
        sectionTicketDetails = findViewById(R.id.section_ticket_details);
        btnAttachPhoto = findViewById(R.id.btn_attach_photo);
        btnSync = findViewById(R.id.btn_sync);

        // Inline details
        textLicPlate = findViewById(R.id.text_lic_plate);
        textState = findViewById(R.id.text_state);
        textMake = findViewById(R.id.text_make);
        textBodyType = findViewById(R.id.text_body_type);
        textColor = findViewById(R.id.text_color);
        textViolation = findViewById(R.id.text_violation);
        textViolDate = findViewById(R.id.text_viol_date);
        textViolTime = findViewById(R.id.text_viol_time);
        textStreet = findViewById(R.id.text_street);
        textCourtDate = findViewById(R.id.text_court_date);
        textCourtTime = findViewById(R.id.text_court_time);
        textCourtCode = findViewById(R.id.text_court_code);

        // Evidence
        recyclerEvidence = findViewById(R.id.recycler_evidence);
        textNoPhotos = findViewById(R.id.text_no_photos);
    }

    private void setupDemoData() {
        demoTickets = new ArrayList<>();

        // Highest ticket number first (last created first)
        demoTickets.add(new Ticket.Builder()
                .setTicketNumber("260148 - NJ | XYZ99")
                .setLicPlate("XYZ99")
                .setState("NJ - NEW JERSEY")
                .setMake("FORD")
                .setBodyType("TRUCK")
                .setColor("WHITE")
                .setViolation("39:4-138 FIRE HYDRANT")
                .setViolDate("02/27/2026")
                .setViolTime("11:45 PM")
                .setCourtDate("03/15/2026")
                .setCourtTime("09:30 AM")
                .setMAppear("N")
                .setTransferStatCode("S")
                .setTransferDT("2026-02-27 23:55:00.000")
                .setCourtCode("1500")
                .setAlphaCode("R22")
                .setSeqNum("260148")
                .setStatusCode("I")
                .setStreet("HIGH ST")
                .build());

        demoTickets.add(new Ticket.Builder()
                .setTicketNumber("260147 - NJ | ABC12")
                .setLicPlate("ABC12")
                .setState("NJ - NEW JERSEY")
                .setMake("HONDA")
                .setBodyType("4 DOOR")
                .setColor("SILVER")
                .setViolation("39:4-98 SPEEDING")
                .setViolDate("02/26/2026")
                .setViolTime("10:15 AM")
                .setCourtDate("03/12/2026")
                .setCourtTime("01:00 PM")
                .setMAppear("N")
                .setTransferStatCode("S")
                .setTransferDT("2026-02-26 10:30:18.000")
                .setCourtCode("1214")
                .setAlphaCode("P15")
                .setSeqNum("260147")
                .setStatusCode("I")
                .setStreet("BROAD ST")
                .build());

        demoTickets.add(new Ticket.Builder()
                .setTicketNumber("260146 - NJ | OUS70")
                .setLicPlate("OUS70")
                .setState("NJ - NEW JERSEY")
                .setMake("ACURA")
                .setBodyType("2 DOOR")
                .setColor("BLUE")
                .setViolation("19:2-3.6 PARKING PROHIBITED")
                .setViolDate("02/25/2026")
                .setViolTime("02:13 PM")
                .setCourtDate("03/04/2026")
                .setCourtTime("09:00 AM")
                .setMAppear("N")
                .setTransferStatCode("S")
                .setTransferDT("2026-02-25 14:19:18.450")
                .setCourtCode("1111")
                .setAlphaCode("D88")
                .setSeqNum("260146")
                .setStatusCode("I")
                .setStreet("MARKET ST")
                .build());
    }

    private void setupSpinner() {
        ArrayAdapter<Ticket> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, demoTickets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTickets.setAdapter(adapter);
        spinnerTickets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTicket = (Ticket) parent.getItemAtPosition(position);
                updateUI(selectedTicket);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupEvidenceRecycler() {
        recyclerEvidence.setLayoutManager(new GridLayoutManager(this, 2));
        evidenceAdapter = new EvidenceAdapter();
        recyclerEvidence.setAdapter(evidenceAdapter);
    }

    private void updateUI(Ticket t) {
        sectionTicketDetails.setVisibility(View.VISIBLE);
        btnAttachPhoto.setVisibility(View.VISIBLE);

        // Vehicle details
        textLicPlate.setText("Lic Plate: " + t.getLicPlate());
        textState.setText("State: " + t.getState());
        textMake.setText("Make: " + t.getMake());
        textBodyType.setText("Body Type: " + t.getBodyType());
        textColor.setText("Color: " + t.getColor());

        // Violation details
        textViolation.setText("Violation: " + t.getViolation());
        textViolDate.setText("Date: " + t.getViolDate());
        textViolTime.setText("Time: " + t.getViolTime());
        textStreet.setText("Street: " + t.getStreet());

        // Court details
        textCourtDate.setText("Court Date: " + t.getCourtDate());
        textCourtTime.setText("Court Time: " + t.getCourtTime());
        textCourtCode.setText("Court Code: " + t.getCourtCode());

        // Observe evidence for this ticket
        observeEvidence(t.getTicketNumber());
    }

    private void observeEvidence(String ticketNumber) {
        db.evidenceDao().getEvidenceForTicket(ticketNumber).observe(this, evidence -> {
            if (evidence != null && !evidence.isEmpty()) {
                textNoPhotos.setVisibility(View.GONE);
                recyclerEvidence.setVisibility(View.VISIBLE);
                evidenceAdapter.setEvidenceList(evidence);
            } else {
                textNoPhotos.setVisibility(View.VISIBLE);
                recyclerEvidence.setVisibility(View.GONE);
                evidenceAdapter.setEvidenceList(new ArrayList<>());
            }
        });
    }

    private void showAttachPhotoOptions() {
        if (selectedTicket == null) {
            Toast.makeText(this, "Please select a ticket first", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Take a Photo", "Select from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Attach Photo Evidence")
                .setItems(options, (dialog, which) -> {
                    Intent intent = new Intent(this, CameraCaptureActivity.class);
                    intent.putExtra("TICKET_ID", selectedTicket.getTicketNumber());
                    if (which == 1) {
                        intent.putExtra("GALLERY_MODE", true);
                    }
                    startActivity(intent);
                })
                .show();
    }

    // Sync logic (from CaseSummaryActivity)
    private void handleSyncAttempt() {
        NetworkUtils.NetworkType type = NetworkUtils.getNetworkType(this);

        if (type == NetworkUtils.NetworkType.NONE) {
            Toast.makeText(this, "No internet connection. Please connect and try again.", Toast.LENGTH_LONG).show();
        } else if (type == NetworkUtils.NetworkType.MOBILE_DATA) {
            showMobileDataWarning();
        } else {
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
        new AlertDialog.Builder(this)
                .setTitle("Cloud Sync: Work In Progress")
                .setMessage("Local evidence validation and storage are complete.\n\nCloud Upload is scheduled for a future sprint.\n\nThank you!")
                .setPositiveButton("Got it", null)
                .show();
    }
}
