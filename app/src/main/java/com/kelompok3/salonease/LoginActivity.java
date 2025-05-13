package com.kelompok3.salonease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelompok3.salonease.databinding.ActivityLoginBinding;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        binding.btnMasuk.setOnClickListener(v -> {
            binding.formLogin.setVisibility(View.VISIBLE);
            binding.formRegister.setVisibility(View.GONE);
            binding.btnMasuk.setSelected(true);
            binding.btnBuatAkun.setSelected(false);
        });

        binding.btnBuatAkun.setOnClickListener(v -> {
            binding.formLogin.setVisibility(View.GONE);
            binding.formRegister.setVisibility(View.VISIBLE);
            binding.btnMasuk.setSelected(false);
            binding.btnBuatAkun.setSelected(true);
        });

        binding.btnBuatAkun.setOnClickListener(v -> {
            binding.formLogin.setVisibility(View.GONE);
            binding.formRegister.setVisibility(View.VISIBLE);
            binding.btnMasuk.setSelected(false);
            binding.btnBuatAkun.setSelected(true);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                binding.loginEmail.setError("Email tidak boleh kosong");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                binding.loginPassword.setError("Password tidak boleh kosong");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                            binding.loginEmail.setText("");
                            binding.loginPassword.setText("");
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.registerName.getText().toString().trim();
            String email = binding.registerEmail.getText().toString().trim();
            String password = binding.registerPassword.getText().toString().trim();
            String confirmPassword = binding.registerConfirm.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                binding.registerName.setError("Nama tidak boleh kosong");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                binding.registerEmail.setError("Email tidak boleh kosong");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                binding.registerPassword.setError("Password tidak boleh kosong");
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                binding.registerConfirm.setError("Konfirmasi password tidak boleh kosong");
                return;
            }
            if (!password.equals(confirmPassword)) {
                binding.registerConfirm.setError("Password tidak cocok");
                return;
            }
            if (password.length() < 6) {
                binding.registerPassword.setError("Password minimal 6 karakter");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Simpan nama ke displayName
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            if (!profileTask.isSuccessful()) {
                                                Toast.makeText(this, "Gagal menyimpan nama ke profil: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });

                                // Simpan nama dan email ke koleksi users di Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                db.collection("users").document(user.getUid())
                                        .set(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(this, "Registrasi berhasil, silakan login", Toast.LENGTH_SHORT).show();
                                                binding.formLogin.setVisibility(View.VISIBLE);
                                                binding.formRegister.setVisibility(View.GONE);
                                                binding.btnMasuk.setSelected(true);
                                                binding.btnBuatAkun.setSelected(false);
                                                binding.registerName.setText("");
                                                binding.registerEmail.setText("");
                                                binding.registerPassword.setText("");
                                                binding.registerConfirm.setText("");
                                            } else {
                                                Toast.makeText(this, "Gagal menyimpan data ke Firestore: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}