package com.kelompok3.salonease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReservationActivity extends AppCompatActivity {

    private TextView tvSelectedServices;
    private CalendarView calendarView;
    private LinearLayout timeButtonsContainer;
    private ImageButton btnBack;
    private Button btnFinish;
    private String selectedDate;
    private Button selectedTimeButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login terlebih dahulu!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inisialisasi elemen UI
        tvSelectedServices = findViewById(R.id.tvSelectedServices);
        calendarView = findViewById(R.id.calendarView);
        timeButtonsContainer = findViewById(R.id.timeButtonsContainer);
        btnBack = findViewById(R.id.btnBack);
        btnFinish = findViewById(R.id.btnFinish);
        db = FirebaseFirestore.getInstance();

        // Tampilkan layanan yang dipilih
        ArrayList<String> selectedServices = getIntent().getStringArrayListExtra("SELECTED_SERVICES");
        if (selectedServices != null && !selectedServices.isEmpty()) {
            StringBuilder servicesText = new StringBuilder("Layanan yang Anda pilih:\n");
            for (String service : selectedServices) {
                servicesText.append("- ").append(service).append("\n");
            }
            tvSelectedServices.setText(servicesText.toString());
        } else {
            tvSelectedServices.setText("Belum ada layanan yang dipilih. Silahkan pilih layanan terlebih dahulu.");
        }

        // Atur kalender dan waktu
        selectedDate = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new Date());
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(java.util.Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            if (selectedTimeButton != null) {
                selectedTimeButton.setSelected(false);
                selectedTimeButton.setElevation(4f);
                selectedTimeButton = null;
            }
        });

        setTimeButtonListeners();

        // Listener untuk tombol kembali
        btnBack.setOnClickListener(v -> onBackPressed());

        // Listener untuk tombol selesai
        btnFinish.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "Sesi login habis, silakan login ulang!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            if (selectedTimeButton == null) {
                Toast.makeText(this, "Silakan pilih waktu reservasi!", Toast.LENGTH_SHORT).show();
            } else if (selectedServices == null || selectedServices.isEmpty()) {
                Toast.makeText(this, "Silakan pilih setidaknya satu layanan di halaman utama!", Toast.LENGTH_LONG).show();
            } else {
                String selectedTime = selectedTimeButton.getText().toString();
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("userId", currentUser.getUid());
                reservation.put("date", selectedDate);
                reservation.put("time", selectedTime);
                reservation.put("services", selectedServices);

                db.collection("reservations")
                        .add(reservation)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Reservasi berhasil disimpan!", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Gagal menyimpan reservasi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        // Atur BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_reservation); // Set item yang aktif
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_reservation) {
                // Sudah di halaman reservasi, tidak perlu pindah
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setTimeButtonListeners() {
        Button[] timeButtons = {
                findViewById(R.id.btn900),
                findViewById(R.id.btn1000),
                findViewById(R.id.btn1100),
                findViewById(R.id.btn1300),
                findViewById(R.id.btn1400),
                findViewById(R.id.btn1500),
                findViewById(R.id.btn1600),
                findViewById(R.id.btn1700),
                findViewById(R.id.btn1800)
        };

        for (Button button : timeButtons) {
            button.setOnClickListener(v -> {
                Button clickedButton = (Button) v;
                if (selectedTimeButton != null && selectedTimeButton != clickedButton) {
                    selectedTimeButton.setSelected(false);
                    selectedTimeButton.setElevation(4f);
                }
                if (selectedTimeButton == clickedButton) {
                    clickedButton.setSelected(false);
                    clickedButton.setElevation(4f);
                    selectedTimeButton = null;
                } else {
                    clickedButton.setSelected(true);
                    clickedButton.setElevation(8f);
                    if (selectedTimeButton != null) {
                        selectedTimeButton.setSelected(false);
                        selectedTimeButton.setElevation(4f);
                    }
                    selectedTimeButton = clickedButton;
                }
                Log.d("ButtonState", "Selected Button: " + (selectedTimeButton != null ? selectedTimeButton.getText() : "None"));
            });
        }
    }
}