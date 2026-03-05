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
import java.util.ArrayList;
import java.util.List;
import edu.njit.njcourts.R;
import edu.njit.njcourts.models.Ticket;

public class TicketSelectionActivity extends AppCompatActivity {

    private Spinner spinnerTickets;
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

        btnShowDetails.setOnClickListener(v -> {
            if (selectedTicket != null && !"Select a Ticket".equals(selectedTicket.getTicketNumber())) {
                showTicketDetailsDialog(selectedTicket);
            }
        });

        btnProceed.setOnClickListener(v -> {
            if (selectedTicket != null && !"Select a Ticket".equals(selectedTicket.getTicketNumber())) {
                Intent intent = new Intent(this, CameraCaptureActivity.class);
                intent.putExtra("TICKET_ID", selectedTicket.getTicketNumber());
                startActivity(intent);
            } else {
                // Show error message as requested
                Toast.makeText(this, "Please select a ticket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        spinnerTickets = findViewById(R.id.spinner_tickets);
        textCarDescription = findViewById(R.id.text_car_description);
        btnProceed = findViewById(R.id.btn_proceed);
        btnShowDetails = findViewById(R.id.btn_show_details);
    }

    private void setupDemoData() {
        demoTickets = new ArrayList<>();
        
        // Placeholder at index 0
        demoTickets.add(new Ticket(
            "Select a Ticket", "", "", "", "", "",
            "", "", "", "", "", 
            "", "", "", "", "", "", "", ""
        ));

        // Ticket 1: From Screenshot
        demoTickets.add(new Ticket(
            "260146 - NJ | OUS70", "OUS70", "NJ - NEW JERSEY", "ACURA", "2 DOOR", "BLUE",
            "19:2-3.6 PARKING PROHIBITED", "02/25/2026", "02:13 PM", "03/04/2026", "09:00 AM", 
            "N", "S", "2026-02-25 14:19:18.450", "1111", "D88", "260146", "I", "MARKET ST"
        ));

        // Ticket 2: Demo
        demoTickets.add(new Ticket(
            "260147 - NJ | ABC12", "ABC12", "NJ - NEW JERSEY", "HONDA", "4 DOOR", "SILVER",
            "39:4-98 SPEEDING", "02/26/2026", "10:15 AM", "03/12/2026", "01:00 PM", 
            "N", "S", "2026-02-26 10:30:18.000", "1214", "P15", "260147", "I", "BROAD ST"
        ));

        // Ticket 3: Demo (Updated from NY to NJ)
        demoTickets.add(new Ticket(
            "260148 - NJ | XYZ99", "XYZ99", "NJ - NEW JERSEY", "FORD", "TRUCK", "WHITE",
            "39:4-138 FIRE HYDRANT", "02/27/2026", "11:45 PM", "03/15/2026", "09:30 AM", 
            "N", "S", "2026-02-27 23:55:00.000", "1500", "R22", "260148", "I", "HIGH ST"
        ));
    }

    private void setupSpinner() {
        ArrayAdapter<Ticket> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, demoTickets);
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

    private void updateUI(Ticket t) {
        if ("Select a Ticket".equals(t.getTicketNumber())) {
            textCarDescription.setText("");
            btnShowDetails.setVisibility(View.GONE);
            return;
        }

        btnShowDetails.setVisibility(View.VISIBLE);

        // Orange description text
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
        summary.append("SUMMARY:\n");
        summary.append(". Lic Plate: ").append(t.getLicPlate()).append("\n");
        summary.append(". State: ").append(t.getState()).append("\n");
        summary.append(". Make: ").append(t.getMake()).append("\n");
        summary.append(". Body Type: ").append(t.getBodyType()).append("\n");
        summary.append(". Color: ").append(t.getColor()).append("\n");
        summary.append(". Violation: ").append(t.getViolation()).append("\n");
        summary.append(". Viol Date: ").append(t.getViolDate()).append("\n");
        summary.append(". Viol Time: ").append(t.getViolTime()).append("\n");
        summary.append(". Court Date: ").append(t.getCourtDate()).append("\n");
        summary.append(". Court Time: ").append(t.getCourtTime()).append("\n");
        summary.append(". Street: ").append(t.getStreet()).append("\n");
        summary.append("****************************************\n");
        summary.append("DETAILS:\n");
        summary.append(". Court Code: ").append(t.getCourtCode()).append("\n");
        summary.append(". Seq Num: ").append(t.getSeqNum()).append("\n");
        summary.append(". Status Code: ").append(t.getStatusCode());
        
        summaryText.setText(summary.toString());

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
