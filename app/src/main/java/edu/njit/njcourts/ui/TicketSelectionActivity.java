package edu.njit.njcourts.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import edu.njit.njcourts.adapters.TicketAdapter;

import edu.njit.njcourts.R;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.TicketEntity;
import edu.njit.njcourts.models.Ticket;

public class TicketSelectionActivity extends AppCompatActivity {

    private Spinner spinnerTickets;
    private RecyclerView recyclerRecentTickets;
    private TextView textCarDescription;
    private Button btnProceed;
    private ImageButton btnShowDetails;
    private List<Ticket> demoTickets;
    private Ticket selectedTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_selection);

        initializeViews();
        setupDemoData();
        setupSpinner();
        setupRecentTickets();
        
        // CRITICAL FIX: Only sync demo data if database is empty
        // This prevents CASCADE delete of photos when tickets are "replaced"
        syncDemoTicketsToDatabaseIfEmpty();

        btnShowDetails.setOnClickListener(v -> {
            if (selectedTicket != null && !"Select a Ticket".equals(selectedTicket.getTicketNumber())) {
                showTicketDetailsDialog(selectedTicket);
            }
        });

        btnProceed.setOnClickListener(v -> {
            if (selectedTicket != null && !"Select a Ticket".equals(selectedTicket.getTicketNumber())) {
                Intent intent = new Intent(this, CaseSummaryActivity.class);
                intent.putExtra("TICKET_ID", selectedTicket.getTicketNumber());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a ticket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncDemoTicketsToDatabaseIfEmpty() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            int count = db.ticketDao().getTicketCountSync();
            if (count == 0) {
                List<TicketEntity> entities = new ArrayList<>();
                for (Ticket t : demoTickets) {
                    if (!"Select a Ticket".equals(t.getTicketNumber())) {
                        entities.add(new TicketEntity(t.getTicketNumber(), t.getViolation(), 
                            t.getColor() + " " + t.getMake(), "SYNCED"));
                    }
                }
                db.ticketDao().insertTickets(entities);
            }
        });
    }

    private void initializeViews() {
        spinnerTickets = findViewById(R.id.spinner_tickets);
        recyclerRecentTickets = findViewById(R.id.recycler_recent_tickets);
        textCarDescription = findViewById(R.id.text_car_description);
        btnProceed = findViewById(R.id.btn_proceed);
        btnShowDetails = findViewById(R.id.btn_show_details);
    }

    private void setupDemoData() {
        demoTickets = new ArrayList<>();
        
        // Use the new Builder Pattern for better readability
        demoTickets.add(new Ticket.Builder().setTicketNumber("Select a Ticket").build());

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

    private void setupRecentTickets() {
        List<Ticket> recentTickets = new ArrayList<>();
        for (int i = 1; i < demoTickets.size(); i++) {
            recentTickets.add(demoTickets.get(i));
        }

        recyclerRecentTickets.setLayoutManager(new LinearLayoutManager(this));
        TicketAdapter recentAdapter = new TicketAdapter(recentTickets, new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                selectedTicket = ticket;
                updateUI(selectedTicket);
                int spinnerIndex = demoTickets.indexOf(ticket);
                if (spinnerIndex >= 0) {
                    spinnerTickets.setSelection(spinnerIndex);
                }
            }

            @Override
            public void onTicketInfoClick(Ticket ticket) {
                showTicketDetailsDialog(ticket);
            }
        });
        recyclerRecentTickets.setAdapter(recentAdapter);
    }

    private void updateUI(Ticket t) {
        if ("Select a Ticket".equals(t.getTicketNumber())) {
            textCarDescription.setText("");
            btnShowDetails.setVisibility(View.GONE);
            btnProceed.setText("TAKE PHOTO"); 
            return;
        }
        btnShowDetails.setVisibility(View.VISIBLE);
        btnProceed.setText("VIEW EVIDENCE");
        String desc = t.getColor() + " " + t.getBodyType() + " " + t.getMake() + " on " + t.getStreet();
        textCarDescription.setText(desc.toUpperCase());
    }

    private void showTicketDetailsDialog(Ticket t) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ticket_details);
        dialog.setCancelable(true);
        TextView title = dialog.findViewById(R.id.text_dialog_title);
        title.setText("Ticket # " + t.getTicketNumber());
        ImageButton closeBtn = dialog.findViewById(R.id.btn_close_dialog);
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        TextView summaryText = dialog.findViewById(R.id.text_full_summary_dialog);
        StringBuilder summary = new StringBuilder();
        summary.append("SUMMARY:\n. Lic Plate: ").append(t.getLicPlate()).append("\n. State: ").append(t.getState()).append("\n. Make: ").append(t.getMake()).append("\n. Body Type: ").append(t.getBodyType()).append("\n. Color: ").append(t.getColor()).append("\n. Violation: ").append(t.getViolation()).append("\n. Street: ").append(t.getStreet()).append("\n****************************************\nDETAILS:\n. Court Code: ").append(t.getCourtCode()).append("\n. Seq Num: ").append(t.getSeqNum()).append("\n. Status Code: ").append(t.getStatusCode());
        summaryText.setText(summary.toString());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
