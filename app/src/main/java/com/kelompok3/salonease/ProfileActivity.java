package com.kelompok3.salonease;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivProfilePicture;
    private ImageButton btnEditPhoto;
    private Button btnLogout;
    private Button btnClearHistory;
    private Button btnDeleteAccount;
    private TextView tvUserName;
    private LinearLayout reservationContainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inisialisasi elemen UI
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        btnLogout = findViewById(R.id.btnLogout);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        tvUserName = findViewById(R.id.tvUserName);
        reservationContainer = findViewById(R.id.reservationContainer);

        // Tampilkan nama pengguna dan riwayat reservasi
        displayUserName(currentUser);
        displayReservationHistory(currentUser.getUid());

        // Listener untuk edit foto
        btnEditPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Listener untuk bersihkan riwayat
        btnClearHistory.setOnClickListener(v -> clearReservationHistory(currentUser.getUid()));

        // Listener untuk logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Listener untuk hapus akun
        btnDeleteAccount.setOnClickListener(v -> deleteUserAccount(currentUser));

        // Atur BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Set item yang aktif
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_reservation) {
                startActivity(new Intent(this, ReservationActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Sudah di halaman profil, tidak perlu pindah
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                ivProfilePicture.setImageURI(imageUri);
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memuat gambar!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void displayUserName(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String name = task.getResult().getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        } else {
                            tvUserName.setText("Pengguna");
                        }
                    } else {
                        tvUserName.setText("Pengguna");
                        Toast.makeText(this, "Gagal memuat nama pengguna", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayReservationHistory(String userId) {
        reservationContainer.removeAllViews();

        db.collection("reservations")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            TextView noHistory = new TextView(this);
                            noHistory.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            noHistory.setText("Tidak ada riwayat reservasi.");
                            noHistory.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
                            noHistory.setTextSize(16f);
                            reservationContainer.addView(noHistory);
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CardView cardView = new CardView(this);
                                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                                cardParams.setMargins(0, 0, 0, 8);
                                cardView.setLayoutParams(cardParams);
                                cardView.setCardElevation(4f);
                                cardView.setRadius(8f);
                                cardView.setPadding(16, 16, 16, 16);
                                cardView.setBackgroundResource(R.drawable.history_card);

                                LinearLayout cardContent = new LinearLayout(this);
                                cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                cardContent.setOrientation(LinearLayout.VERTICAL);

                                TextView tvReservation = new TextView(this);
                                tvReservation.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                String date = Objects.requireNonNull(document.getString("date"));
                                String time = Objects.requireNonNull(document.getString("time"));
                                tvReservation.setText("Tanggal: " + date + ", Waktu: " + time);
                                tvReservation.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
                                tvReservation.setTextSize(16f);

                                TextView tvServices = new TextView(this);
                                tvServices.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                tvServices.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
                                tvServices.setTextSize(14f);

                                ArrayList<String> services = (ArrayList<String>) document.get("services");
                                if (services != null && !services.isEmpty()) {
                                    StringBuilder servicesText = new StringBuilder("Layanan:\n");
                                    for (String service : services) {
                                        servicesText.append("- ").append(service).append("\n");
                                    }
                                    tvServices.setText(servicesText.toString().trim());
                                } else {
                                    tvServices.setText("Layanan: Tidak ada layanan yang dipilih.");
                                }

                                cardContent.addView(tvReservation);
                                cardContent.addView(tvServices);
                                cardView.addView(cardContent);
                                reservationContainer.addView(cardView);
                            }
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        if (errorMessage.contains("FAILED_PRECONDITION")) {
                            Toast.makeText(this, "Kueri memerlukan indeks. Silakan buat indeks di Firebase Console.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Gagal memuat riwayat: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void clearReservationHistory(String userId) {
        db.collection("reservations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Toast.makeText(this, "Riwayat reservasi berhasil dibersihkan!", Toast.LENGTH_SHORT).show();
                        displayReservationHistory(userId);
                    } else {
                        Toast.makeText(this, "Gagal membersihkan riwayat: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteUserAccount(FirebaseUser user) {
        String userId = user.getUid();

        db.collection("users").document(userId)
                .delete()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        db.collection("reservations")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnCompleteListener(reservationTask -> {
                                    if (reservationTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : reservationTask.getResult()) {
                                            document.getReference().delete();
                                        }
                                        user.delete()
                                                .addOnCompleteListener(authTask -> {
                                                    if (authTask.isSuccessful()) {
                                                        Toast.makeText(this, "Akun dan data berhasil dihapus", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(this, LoginActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(this, "Gagal menghapus akun: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(this, "Gagal menghapus reservasi: " + reservationTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Gagal menghapus data pengguna: " + userTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}