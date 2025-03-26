package com.lapTrinhUUDD.movie.ui.signup;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.lapTrinhUUDD.movie.R;
import com.lapTrinhUUDD.movie.User.User;
import com.lapTrinhUUDD.movie.ui.login.LoginActivity;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText signupUsername, signupEmail, signupPassword, signupConfirmPassword, signupDob;
    private TextView loginRedirectText;
    private Button signupButton;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale;
    private ImageView showSignupPassword, showConfirmPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        signupUsername = findViewById(R.id.signup_username);
        signupEmail = findViewById(R.id.signup_email);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirmpassword);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        showSignupPassword = findViewById(R.id.show_hide_signup_password);
        showConfirmPassword = findViewById(R.id.show_hide_signup_confirmpass);

        showSignupPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signupPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showSignupPassword.setImageResource(R.drawable.icon_show);
                }else{
                    signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showSignupPassword.setImageResource(R.drawable.icon_hide);
                }
            }
        });

        showConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signupConfirmPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    signupConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showConfirmPassword.setImageResource(R.drawable.icon_show);
                }else{
                    signupConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showConfirmPassword.setImageResource(R.drawable.icon_hide);
                }
            }
        });

        signupDob = findViewById(R.id.signup_dateofbirth);
        showSignupPassword = findViewById(R.id.show_hide_signup_password);
        showConfirmPassword = findViewById(R.id.show_hide_signup_confirmpass);

        showSignupPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signupPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showSignupPassword.setImageResource(R.drawable.icon_show);
                }else{
                    signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showSignupPassword.setImageResource(R.drawable.icon_hide);
                }
            }
        });

        showConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signupConfirmPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    signupConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showConfirmPassword.setImageResource(R.drawable.icon_show);
                }else{
                    signupConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showConfirmPassword.setImageResource(R.drawable.icon_hide);
                }
            }
        });
        signupDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Tạo và hiển thị DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SignUpActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Định dạng ngày đã chọn và hiển thị trong EditText
                            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            signupDob.setText(selectedDate);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = signupUsername.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();
                String confirmPassword = signupConfirmPassword.getText().toString().trim();
                String dob = signupDob.getText().toString().trim();
                String gender = "";

                int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();

                if (selectedGenderId == R.id.radioMale) {
                    gender = "Male";
                } else if (selectedGenderId == R.id.radioFemale) {
                    gender = "Female";
                }

                if(username.isEmpty()){
                    signupUsername.setError("Username cannot be empty");
                }

                if(email.isEmpty()){
                    signupEmail.setError("Email cannot be empty");
                }

                if(dob.isEmpty()){
                    signupDob.setError("Date of birth cannot be empty");
                }

                if(gender.equals("")){
                    Toast.makeText(SignUpActivity.this, "Gender cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(dob.isEmpty()){
                    signupDob.setError("Date of birth cannot be empty");
                }

                if(password.isEmpty()){
                    signupPassword.setError("Password cannot be empty");
                }

                if(confirmPassword.isEmpty()){
                    signupConfirmPassword.setError("Confirm password cannot be empty");
                }

                if(confirmPassword.equals(password)){
                    String finalGender = gender;
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                User user = new User(username, email, dob, finalGender,"");
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(auth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SignUpActivity.this, "User has been registered successfully", Toast.LENGTH_LONG).show();
                                                    auth.signOut();
                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                }else{
                                                    Toast.makeText(SignUpActivity.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(SignUpActivity.this, "SignUp Failed Beacause " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(SignUpActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}