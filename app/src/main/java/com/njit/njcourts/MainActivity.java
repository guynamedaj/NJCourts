package com.njit.njcourts;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import edu.njit.njcourts.ui.TicketSelectionActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Redirect to the new UI package
        Intent intent = new Intent(this, TicketSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
