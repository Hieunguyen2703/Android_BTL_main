package com.lapTrinhUUDD.movie.User;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lapTrinhUUDD.movie.Models.User;
import com.lapTrinhUUDD.movie.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class UpdateProfileActivity extends AppCompatActivity {
    private ImageView backButton, logoutBtn;
    private TextView title;
    private String fullname, email, dob, gender;
    private String udFullname, udDob, udGender, udImg;
    private EditText updateName, updateDob;
    private RadioGroup radioButtonGender;
    private RadioButton radioButtonGenderUpdate;
    private FirebaseAuth userProfile;
    private Button btnUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        iniActionBar();
        iniView();
        updateDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Tạo và hiển thị DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        UpdateProfileActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Định dạng ngày đã chọn và hiển thị trong EditText
                            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            updateDob.setText(selectedDate);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference("users");
        userProfile = FirebaseAuth.getInstance();
        FirebaseUser user = userProfile.getCurrentUser();

        showprofile(user,profileReference);

        btnUpdate.setOnClickListener(v -> updateProfileToFirebase(user,profileReference));


    }

    private void showprofile(FirebaseUser user, DatabaseReference profileReference) {


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        profileReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if(user != null){
                    fullname = user.getUsername();
                    email = user.getEmail();
                    dob = user.getDob();
                    gender = user.getGender();
                    udImg = user.getImg();

                    updateName.setText(fullname);
                    updateDob.setText(dob);

                    if(gender.equals("Male")){
                        radioButtonGenderUpdate = findViewById(R.id.radioMale);
                    }else if(gender.equals("Female")){
                        radioButtonGenderUpdate = findViewById(R.id.radioFemale);
                    }
                    radioButtonGenderUpdate.setChecked(true);
                }else{
                    Toast.makeText(UpdateProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void updateProfileToFirebase(FirebaseUser userref, DatabaseReference profileReference) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        udFullname = updateName.getText().toString().trim();
        udDob = updateDob.getText().toString().trim();
        udGender = radioButtonGender.getCheckedRadioButtonId() == R.id.radioMale ? "Male" : "Female";

        if(udFullname.isEmpty()){
            updateName.setError("Username cannot be empty");
        }

        if(udGender.equals("")){
            Toast.makeText(this, "Gender cannot be empty", Toast.LENGTH_SHORT).show();
        }

        if(udDob.isEmpty()){
            updateDob.setError("Date of birth cannot be empty");
        }

        User user = new User(udFullname, userref.getEmail(), udDob, udGender, udImg);
        profileReference.child(userref.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(UpdateProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateProfileActivity.this, UserProfileActivity.class));
                }else{
                    Toast.makeText(UpdateProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private void iniView() {
        updateName = findViewById(R.id.profile_username);
        updateDob = findViewById(R.id.profile_dateofbirth);
        radioButtonGender = findViewById(R.id.radioGroupGender);
        btnUpdate = findViewById(R.id.update_button);
    }

    private void iniActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar_profile);

            title = findViewById(R.id.title);
            title.setText("Update Profile");

            backButton = findViewById(R.id.btn_back);
            backButton.setOnClickListener(v -> startActivity(new Intent(UpdateProfileActivity.this, UserProfileActivity.class)));

            logoutBtn = findViewById(R.id.btn_logout);
            logoutBtn.setVisibility(View.GONE);
        }
    }
}