package edu.njit.njcourts.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.concurrent.Executors;

import edu.njit.njcourts.R;
import edu.njit.njcourts.data.ApiTicket;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.RetrofitClient;
import edu.njit.njcourts.data.TicketEntity;
import edu.njit.njcourts.models.Ticket;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketSelectionActivity extends AppCompatActivity {

    private static final String TAG = "TicketSelectionActivity";
    private static final String PLACEHOLDER = "Select a Ticket";

    private Spinner spinnerTickets;
    private TextView textCarDescription;
    private Button btnProceed;
    private ImageButton btnShowDetails;
    private List<Ticket> tickets;
    private ArrayAdapter<Ticket> spinnerAdapter;
    private Ticket selectedTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_selection);

        initializeViews();

        tickets = new ArrayList<>();
        tickets.add(new Ticket.Builder().setTicketNumber(PLACEHOLDER).build());
        setupSpinner();

        fetchTicketsFromBackend();

        btnShowDetails.setOnClickListener(v -> {
            if (selectedTicket != null && !PLACEHOLDER.equals(selectedTicket.getTicketNumber())) {
                showTicketDetailsDialog(selectedTicket);
            }
        });

        btnProceed.setOnClickListener(v -> {
            if (selectedTicket != null && !PLACEHOLDER.equals(selectedTicket.getTicketNumber())) {
                Intent intent = new Intent(this, CaseSummaryActivity.class);
                intent.putExtra("TICKET_ID", selectedTicket.getTicketNumber());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a ticket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTicketsFromBackend() {
        RetrofitClient.get().getTickets().enqueue(new Callback<List<ApiTicket>>() {
            @Override
            public void onResponse(Call<List<ApiTicket>> call, Response<List<ApiTicket>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "getTickets HTTP " + response.code());
                    Toast.makeText(TicketSelectionActivity.this,
                        "Failed to load tickets (HTTP " + response.code() + ")",
                        Toast.LENGTH_LONG).show();
                    return;
                }
                populateFromApi(response.body());
            }

            @Override
            public void onFailure(Call<List<ApiTicket>> call, Throwable t) {
                Log.e(TAG, "getTickets failed", t);
                Toast.makeText(TicketSelectionActivity.this,
                    "Could not reach server: " + t.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateFromApi(List<ApiTicket> apiTickets) {
        for (ApiTicket a : apiTickets) {
            tickets.add(toDisplayTicket(a));
        }
        spinnerAdapter.notifyDataSetChanged();

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
            AppDatabase.getDatabase(getApplicationContext()).ticketDao().insertTickets(entities)
        );
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

    private void initializeViews() {
        spinnerTickets = findViewById(R.id.spinner_tickets);
        textCarDescription = findViewById(R.id.text_car_description);
        btnProceed = findViewById(R.id.btn_proceed);
        btnShowDetails = findViewById(R.id.btn_show_details);
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

    private void updateUI(Ticket t) {
        if (PLACEHOLDER.equals(t.getTicketNumber())) {
            textCarDescription.setText("");
            btnShowDetails.setVisibility(View.GONE);
            btnProceed.setText("TAKE PHOTO");
            return;
        }
        btnShowDetails.setVisibility(View.VISIBLE);
        btnProceed.setText("VIEW EVIDENCE");
        String desc = nullSafe(t.getColor()) + " " + nullSafe(t.getBodyType()) + " "
            + nullSafe(t.getMake()) + " on " + nullSafe(t.getStreet());
        textCarDescription.setText(desc.trim().toUpperCase());
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
        summary.append("SUMMARY:\n. Lic Plate: ").append(t.getLicPlate())
            .append("\n. State: ").append(t.getState())
            .append("\n. Make: ").append(t.getMake())
            .append("\n. Body Type: ").append(t.getBodyType())
            .append("\n. Color: ").append(t.getColor())
            .append("\n. Violation: ").append(t.getViolation())
            .append("\n. Street: ").append(t.getStreet());
        summaryText.setText(summary.toString());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
