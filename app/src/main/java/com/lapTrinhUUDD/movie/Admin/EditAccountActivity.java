package com.lapTrinhUUDD.movie.Admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lapTrinhUUDD.movie.Models.User;
import com.lapTrinhUUDD.movie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditAccountActivity extends AppCompatActivity {

    EditText etUsername, etEmail, etDob;
    Spinner spinnerGender;
    Button btnSave;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        String username = getIntent().getStringExtra("username");

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etDob = findViewById(R.id.etDob);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Khởi tạo adapter cho Spinner (giới tính)
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.genders_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            etUsername.setText(user.getUsername());
                            etEmail.setText(user.getEmail());
                            etDob.setText(user.getDob());

                            // Chọn giá trị cho Spinner Gender
                            if (user.getGender() != null) {
                                int genderPosition = genderAdapter.getPosition(user.getGender());
                                spinnerGender.setSelection(genderPosition);
                            }

                            btnSave.setOnClickListener(v -> {
                                String newUsername = etUsername.getText().toString().trim();
                                String newEmail = etEmail.getText().toString().trim();
                                String newDob = etDob.getText().toString().trim();
                                String newGender = spinnerGender.getSelectedItem().toString().trim();

                                if (!newUsername.isEmpty()) {
                                    // Update user information
                                    snapshot.getRef().child("username").setValue(newUsername);
                                    snapshot.getRef().child("email").setValue(newEmail);
                                    snapshot.getRef().child("dob").setValue(newDob);
                                    snapshot.getRef().child("gender").setValue(newGender);

                                    Toast.makeText(EditAccountActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
                                    finish(); // Đóng activity sau khi lưu thay đổi
                                } else {
                                    etUsername.setError("Username cannot be empty");
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(EditAccountActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng activity nếu không tìm thấy người dùng
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditAccountActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
