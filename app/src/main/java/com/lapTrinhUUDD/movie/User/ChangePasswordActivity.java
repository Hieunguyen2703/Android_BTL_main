package com.lapTrinhUUDD.movie.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.lapTrinhUUDD.movie.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView backButton, logoutBtn, showCurrentPass, showNewPass, showConfirmPass;
    private TextView title;
    private String currentPassword, password, confirmPass;
    private EditText oldPassword, newPassword, confirmPassword;
    private Button submitBtn;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        iniActionBar();
        iniView();
        submitBtn.setOnClickListener(v -> {changePassword();});
        showHidePass();

    }

    private void showHidePass() {
        showCurrentPass = findViewById(R.id.show_hide_current_password);
        showNewPass = findViewById(R.id.show_hide_new_password);
        showConfirmPass = findViewById(R.id.show_hide_confirm_password);

        showCurrentPass.setOnClickListener(v -> {
            if(oldPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                oldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showCurrentPass.setImageResource(R.drawable.icon_show);
            }else{
                oldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showCurrentPass.setImageResource(R.drawable.icon_hide);
            }
        });
        showNewPass.setOnClickListener(v -> {
            if(newPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showNewPass.setImageResource(R.drawable.icon_show);
            }else{
                newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showNewPass.setImageResource(R.drawable.icon_hide);
            }
        });
        showConfirmPass.setOnClickListener(v -> {
            if(confirmPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showConfirmPass.setImageResource(R.drawable.icon_show);
            }else{
                confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showConfirmPass.setImageResource(R.drawable.icon_hide);
            }
        });

    }

    private void changePassword() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        currentPassword = oldPassword.getText().toString().trim();
        password = newPassword.getText().toString().trim();
        confirmPass = confirmPassword.getText().toString().trim();

        if(currentPassword.isEmpty()){
            oldPassword.setError("Current Password is required");
            return;
        }
        if(password.isEmpty()){
            newPassword.setError("New Password is required");
            return;
        }
        if(confirmPass.isEmpty()){
            confirmPassword.setError("Confirm Password is required");
            return;
        }

        if(password.equals(confirmPass)){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> taskChangePassword) {
                                if(taskChangePassword.isSuccessful()){
                                    Toast.makeText(ChangePasswordActivity.this, "Change password successful", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }else{
                                    Toast.makeText(ChangePasswordActivity.this, taskChangePassword.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(ChangePasswordActivity.this, "Error authentication failed, please check your current password", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }else {
            Toast.makeText(this, "Password and Confirm Password does not match", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniView() {
        oldPassword = findViewById(R.id.current_password);
        newPassword = findViewById(R.id.new_password);
        confirmPassword = findViewById(R.id.confirm_password);
        submitBtn = findViewById(R.id.submit_button);
    }

    private void iniActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar_profile);

            title = findViewById(R.id.title);
            title.setText("Change Password");

            backButton = findViewById(R.id.btn_back);
            backButton.setOnClickListener(v -> startActivity(new Intent(ChangePasswordActivity.this, UserProfileActivity.class)));

            logoutBtn = findViewById(R.id.btn_logout);
            logoutBtn.setVisibility(View.GONE);
        }
    }
}