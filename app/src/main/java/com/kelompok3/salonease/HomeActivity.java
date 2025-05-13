package com.kelompok3.salonease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private CheckBox cbHairStyle, cbHairSpa, cbShampoo, cbHairDryer, cbFacial, cbMakeup;
    private Button btnReservasi;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        cbHairStyle = findViewById(R.id.cbHairStyle);
        cbHairSpa = findViewById(R.id.cbHairSpa);
        cbShampoo = findViewById(R.id.cbShampoo);
        cbHairDryer = findViewById(R.id.cbHairDryer);
        cbFacial = findViewById(R.id.cbFacial);
        cbMakeup = findViewById(R.id.cbMakeup);

        btnReservasi = findViewById(R.id.btnReservasi);
        btnReservasi.setOnClickListener(v -> handleReservation());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Pastikan Home dipilih saat aktivitas dimuat
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_reservation) {
                startActivity(new Intent(this, ReservationActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void handleReservation() {
        ArrayList<String> selectedServices = new ArrayList<>();
        if (cbHairStyle.isChecked()) selectedServices.add(cbHairStyle.getText().toString());
        if (cbHairSpa.isChecked()) selectedServices.add(cbHairSpa.getText().toString());
        if (cbShampoo.isChecked()) selectedServices.add(cbShampoo.getText().toString());
        if (cbHairDryer.isChecked()) selectedServices.add(cbHairDryer.getText().toString());
        if (cbFacial.isChecked()) selectedServices.add(cbFacial.getText().toString());
        if (cbMakeup.isChecked()) selectedServices.add(cbMakeup.getText().toString());

        if (selectedServices.isEmpty()) {
            Toast.makeText(this, "Pilih setidaknya satu layanan!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ReservationActivity.class);
        intent.putStringArrayListExtra("SELECTED_SERVICES", selectedServices);
        startActivity(intent);
    }
}