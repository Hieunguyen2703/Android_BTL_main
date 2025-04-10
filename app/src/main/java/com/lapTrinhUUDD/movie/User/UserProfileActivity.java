package com.lapTrinhUUDD.movie.User;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.lapTrinhUUDD.movie.Models.User;
import com.lapTrinhUUDD.movie.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {
    private TextView fullnameTV, emailTV, dobTV, genderTV;
    private FirebaseAuth mAuth;
    private String fullname, email, dob, gender;
    ImageView accountImage, backBtn, logoutBtn;
    Button updateBtn, changePasswordBtn;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniActionBar();

        fullnameTV = findViewById(R.id.fullname);
        emailTV = findViewById(R.id.email);
        dobTV = findViewById(R.id.dob);
        genderTV = findViewById(R.id.gender);
        accountImage = findViewById(R.id.img_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            showUserProfiles(user);
        }else{
            Toast.makeText(this, "User detail in not available", Toast.LENGTH_SHORT).show();
        }

        updateBtn = findViewById(R.id.update_profile);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfileActivity.this, UpdateProfileActivity.class));
            }
        });
        changePasswordBtn = findViewById(R.id.change_password);
        changePasswordBtn.setOnClickListener(v -> {startActivity(new Intent(UserProfileActivity.this, ChangePasswordActivity.class));});
    }

    private void iniActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar_profile);

            backBtn = findViewById(R.id.btn_back);
            logoutBtn = findViewById(R.id.btn_logout);
            builder = new AlertDialog.Builder(this);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
                }
            });

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.setTitle("Logout")
                            .setMessage("Are you sure you want to log out?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            });
        }
    }

    private void showUserProfiles(FirebaseUser user) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        String uid = user.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("users");
        referenceProfile.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if(user != null){
                    fullname = user.getUsername();
                    email = user.getEmail();
                    dob = user.getDob();
                    gender = user.getGender();

                    fullnameTV.setText(fullname);
                    emailTV.setText(email);
                    dobTV.setText(dob);
                    genderTV.setText(gender);

                    if (user.getImg() != null && !user.getImg().isEmpty()) {
                        Glide.with(UserProfileActivity.this).load(user.getImg()).into(accountImage);
                    }


                }
                progressDialog.dismiss();

                accountImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UserProfileActivity.this, ProfileImageActivity.class);
                        intent.putExtra("imageUri", user.getImg());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

}