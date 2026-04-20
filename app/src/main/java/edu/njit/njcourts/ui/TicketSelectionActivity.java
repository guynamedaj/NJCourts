package edu.njit.njcourts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import edu.njit.njcourts.adapters.EvidenceItem;
import edu.njit.njcourts.data.ApiEvidence;
import edu.njit.njcourts.data.ApiTicket;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.PhotoEvidenceEntity;
import edu.njit.njcourts.data.RetrofitClient;
import edu.njit.njcourts.data.TicketEntity;
import edu.njit.njcourts.models.Ticket;
import edu.njit.njcourts.utils.NetworkUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketSelectionActivity extends AppCompatActivity {

    private static final String TAG = "TicketSelectionActivity";

    private Spinner spinnerTickets;
    private View sectionTicketDetails;
    private Button btnAttachPhoto;
    private Button btnSync;
    private List<Ticket> tickets;
    private ArrayAdapter<Ticket> spinnerAdapter;
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

    // Remote evidence cache for merging with local
    private List<EvidenceItem> remoteItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_selection);

        db = AppDatabase.getDatabase(this);

        initializeViews();

        tickets = new ArrayList<>();
        setupSpinner();
        setupEvidenceRecycler();

        // Load demo data immediately, then try API (replaces if successful)
        loadDemoTickets();
        fetchTicketsFromBackend();

        btnAttachPhoto.setOnClickListener(v -> showAttachPhotoOptions());
        btnSync.setOnClickListener(v -> handleSyncAttempt());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedTicket != null) {
            observeEvidence(selectedTicket.getTicketNumber());
        }
    }

    // ── API Fetching ──────────────────────────────────────────────

    private void fetchTicketsFromBackend() {
        RetrofitClient.get().getTickets().enqueue(new Callback<List<ApiTicket>>() {
            @Override
            public void onResponse(Call<List<ApiTicket>> call, Response<List<ApiTicket>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "getTickets HTTP " + response.code());
                    return; // Keep demo data
                }
                populateFromApi(response.body());
            }

            @Override
            public void onFailure(Call<List<ApiTicket>> call, Throwable t) {
                Log.e(TAG, "getTickets failed, using demo data", t);
            }
        });
    }

    private void populateFromApi(List<ApiTicket> apiTickets) {
        tickets.clear();
        for (ApiTicket a : apiTickets) {
            tickets.add(toDisplayTicket(a));
        }
        spinnerAdapter.notifyDataSetChanged();

        if (!tickets.isEmpty()) {
            spinnerTickets.setSelection(0);
        }

        // Sync tickets to Room
        final List<TicketEntity> entities = new ArrayList<>();
        for (ApiTicket a : apiTickets) {
            entities.add(new TicketEntity(
                a.ticketNumber,
                nullSafe(a.violationType),
                buildVehicleSummary(a),
                "SYNCED"
            ));
        }
        Executors.newSingleThreadExecutor().execute(() ->
            db.ticketDao().insertTickets(entities)
        );
    }

    private void loadDemoTickets() {
        tickets.clear();

        tickets.add(new Ticket.Builder()
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
                .setCourtCode("1500")
                .setStreet("HIGH ST")
                .build());

        tickets.add(new Ticket.Builder()
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
                .setCourtCode("1214")
                .setStreet("BROAD ST")
                .build());

        tickets.add(new Ticket.Builder()
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
                .setCourtCode("1111")
                .setStreet("MARKET ST")
                .build());

        spinnerAdapter.notifyDataSetChanged();

        // Sync demo tickets to Room if empty
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = db.ticketDao().getTicketCountSync();
            if (count == 0) {
                List<TicketEntity> entities = new ArrayList<>();
                for (Ticket t : tickets) {
                    entities.add(new TicketEntity(t.getTicketNumber(), t.getViolation(),
                        t.getColor() + " " + t.getMake(), "SYNCED"));
                }
                db.ticketDao().insertTickets(entities);
            }
        });
    }

    private Ticket toDisplayTicket(ApiTicket a) {
        return new Ticket.Builder()
            .setTicketNumber(a.ticketNumber)
            .setLicPlate(nullSafe(a.licensePlate))
            .setState(nullSafe(a.plateState))
            .setMake(nullSafe(a.vehicleMake))
            .setBodyType(nullSafe(a.vehicleModel))
            .setColor(nullSafe(a.vehicleColor))
            .setViolation(nullSafe(a.violationType))
            .setStreet(nullSafe(a.location))
            .build();
    }

    private String buildVehicleSummary(ApiTicket a) {
        StringBuilder sb = new StringBuilder();
        if (a.vehicleColor != null) sb.append(a.vehicleColor).append(' ');
        if (a.vehicleMake != null) sb.append(a.vehicleMake).append(' ');
        if (a.vehicleModel != null) sb.append(a.vehicleModel);
        return sb.toString().trim();
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    // ── Views ─────────────────────────────────────────────────────

    private void initializeViews() {
        spinnerTickets = findViewById(R.id.spinner_tickets);
        sectionTicketDetails = findViewById(R.id.section_ticket_details);
        btnAttachPhoto = findViewById(R.id.btn_attach_photo);
        btnSync = findViewById(R.id.btn_sync);

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

        recyclerEvidence = findViewById(R.id.recycler_evidence);
        textNoPhotos = findViewById(R.id.text_no_photos);
    }

    private void setupSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tickets);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTickets.setAdapter(spinnerAdapter);
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

        textLicPlate.setText("Lic Plate: " + t.getLicPlate());
        textState.setText("State: " + t.getState());
        textMake.setText("Make: " + t.getMake());
        textBodyType.setText("Body Type: " + t.getBodyType());
        textColor.setText("Color: " + t.getColor());

        textViolation.setText("Violation: " + t.getViolation());
        textViolDate.setText("Date: " + t.getViolDate());
        textViolTime.setText("Time: " + t.getViolTime());
        textStreet.setText("Street: " + t.getStreet());

        textCourtDate.setText("Court Date: " + t.getCourtDate());
        textCourtTime.setText("Court Time: " + t.getCourtTime());
        textCourtCode.setText("Court Code: " + t.getCourtCode());

        // Load evidence (local + remote)
        remoteItems.clear();
        observeEvidence(t.getTicketNumber());
        fetchRemoteEvidence(t.getTicketNumber());
    }

    // ── Evidence ──────────────────────────────────────────────────

    private void observeEvidence(String ticketNumber) {
        db.evidenceDao().getEvidenceForTicket(ticketNumber).observe(this, localPhotos -> {
            List<EvidenceItem> localItems = new ArrayList<>();
            if (localPhotos != null) {
                for (PhotoEvidenceEntity photo : localPhotos) {
                    localItems.add(EvidenceItem.fromLocal(photo));
                }
            }
            rebuildEvidenceList(localItems);
        });
    }

    private void fetchRemoteEvidence(String ticketNumber) {
        RetrofitClient.get().getTicketEvidence(ticketNumber).enqueue(new Callback<List<ApiEvidence>>() {
            @Override
            public void onResponse(Call<List<ApiEvidence>> call, Response<List<ApiEvidence>> response) {
                remoteItems.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (ApiEvidence e : response.body()) {
                        remoteItems.add(EvidenceItem.fromRemote(e));
                    }
                }
                // Re-trigger local observation to merge
                observeEvidence(ticketNumber);
            }

            @Override
            public void onFailure(Call<List<ApiEvidence>> call, Throwable t) {
                Log.w(TAG, "Failed to fetch remote evidence", t);
            }
        });
    }

    private void rebuildEvidenceList(List<EvidenceItem> localItems) {
        List<EvidenceItem> merged = new ArrayList<>(localItems);
        for (EvidenceItem remote : remoteItems) {
            boolean isDuplicate = false;
            for (EvidenceItem local : localItems) {
                if (local.fileName != null && local.fileName.equals(remote.fileName)
                        && local.timestampMs == remote.timestampMs) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                merged.add(remote);
            }
        }

        if (!merged.isEmpty()) {
            textNoPhotos.setVisibility(View.GONE);
            recyclerEvidence.setVisibility(View.VISIBLE);
            evidenceAdapter.setItems(merged);
        } else {
            textNoPhotos.setVisibility(View.VISIBLE);
            recyclerEvidence.setVisibility(View.GONE);
            evidenceAdapter.setItems(new ArrayList<>());
        }
    }

    // ── Attach Photo ──────────────────────────────────────────────

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

    // ── Sync / Upload ─────────────────────────────────────────────

    private void handleSyncAttempt() {
        if (selectedTicket == null) {
            Toast.makeText(this, "Please select a ticket first", Toast.LENGTH_SHORT).show();
            return;
        }

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
        String ticketNumber = selectedTicket.getTicketNumber();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<PhotoEvidenceEntity> unsynced = db.evidenceDao().getUnsyncedEvidenceSync(ticketNumber);
            if (unsynced == null || unsynced.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this,
                    "All photos already synced!", Toast.LENGTH_SHORT).show());
                return;
            }

            runOnUiThread(() -> Toast.makeText(this,
                "Uploading " + unsynced.size() + " photo(s)...", Toast.LENGTH_SHORT).show());

            int successCount = 0;
            for (PhotoEvidenceEntity photo : unsynced) {
                try {
                    RequestBody ticketPart = RequestBody.create(
                        MediaType.parse("text/plain"), ticketNumber);
                    RequestBody timestampPart = RequestBody.create(
                        MediaType.parse("text/plain"),
                        String.valueOf(photo.timestamp));
                    RequestBody fileBody = RequestBody.create(
                        MediaType.parse("image/jpeg"), photo.photoBlob);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                        "photo",
                        photo.originalFilename != null ? photo.originalFilename : "evidence.jpg",
                        fileBody);

                    Response<ResponseBody> resp = RetrofitClient.get()
                        .uploadPhoto(ticketPart, timestampPart, filePart)
                        .execute();

                    if (resp.isSuccessful()) {
                        db.evidenceDao().updateSyncStatus(photo.id, "SYNCED");
                        successCount++;
                    } else {
                        Log.w(TAG, "Upload failed HTTP " + resp.code());
                        db.evidenceDao().updateSyncStatus(photo.id, "FAILED");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Upload exception for photo " + photo.id, e);
                    db.evidenceDao().updateSyncStatus(photo.id, "FAILED");
                }
            }

            final int s = successCount;
            final int total = unsynced.size();
            runOnUiThread(() -> {
                Toast.makeText(this,
                    s + "/" + total + " photos uploaded successfully",
                    Toast.LENGTH_LONG).show();
                fetchRemoteEvidence(ticketNumber);
            });
        });
    }
}
