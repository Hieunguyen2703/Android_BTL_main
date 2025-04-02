package com.lapTrinhUUDD.movie.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lapTrinhUUDD.movie.R;
import com.lapTrinhUUDD.movie.User.HomeActivity;
import com.lapTrinhUUDD.movie.User.MainActivity;

public class AdminHomeActivity extends AppCompatActivity {
    Button btn,btnThoat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
         btn =findViewById(R.id.button);
         btnThoat = findViewById(R.id.btnthoat);
         btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(AdminHomeActivity.this, UploadVideoActivity.class));
                 finish();
             }
         });
         btnThoat.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(AdminHomeActivity.this, HomeActivity.class));
                 finish();
             }
         });

    }
}