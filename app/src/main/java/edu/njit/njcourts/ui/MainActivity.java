package edu.njit.njcourts.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import edu.njit.njcourts.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Task 9: Finalize Mobile UI Flow
        // For POC, we start with Ticket Selection
        Intent intent = new Intent(this, TicketSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
