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

import com.lapTrinhUUDD.movie.Models.GetVideoDetails;
import com.lapTrinhUUDD.movie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditFilmActivity extends AppCompatActivity {

    EditText etVideoName, etVideoDescription;
    Spinner etVideoCategory, etVideoType;
    Button btnSave;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_film);

        String videoName = getIntent().getStringExtra("video_name");

        etVideoName = findViewById(R.id.etVideoName);
        etVideoDescription = findViewById(R.id.etVideoDescription);
        etVideoCategory = findViewById(R.id.etVideoCategory);
        etVideoType = findViewById(R.id.etVideoType);
        btnSave = findViewById(R.id.btnSaveChanges);
        btnBack = findViewById(R.id.btnBack);

        String[] categories = {"Action", "Adventure", "Comedy", "Romantic", "Sport"};
        String[] types = {"Latest Movies", "Best Popular Movies"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etVideoCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etVideoType.setAdapter(typeAdapter);

        String videoCategory = getIntent().getStringExtra("videoCategory");
        String videoType = getIntent().getStringExtra("videoType");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");

        databaseReference.orderByChild("video_name").equalTo(videoName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        GetVideoDetails video = snapshot.getValue(GetVideoDetails.class);
                        if (video != null) {
                            etVideoName.setText(video.getVideo_name());
                            etVideoDescription.setText(video.getVideo_description());
                            if (videoCategory != null) {
                                int categoryPosition = categoryAdapter.getPosition(videoCategory);
                                etVideoCategory.setSelection(categoryPosition);
                            }
                            if (videoType != null) {
                                int typePosition = typeAdapter.getPosition(videoType);
                                etVideoType.setSelection(typePosition);
                            }

                            btnSave.setOnClickListener(v -> {
                                String newName = etVideoName.getText().toString().trim();
                                String newDescription = etVideoDescription.getText().toString().trim();
                                String newCategory = etVideoCategory.getSelectedItem().toString().trim();
                                String newType = etVideoType.getSelectedItem().toString().trim();

                                if (!newName.isEmpty()) {
                                    snapshot.getRef().child("video_name").setValue(newName);
                                    snapshot.getRef().child("video_description").setValue(newDescription);
                                    snapshot.getRef().child("video_category").setValue(newCategory);
                                    snapshot.getRef().child("video_type").setValue(newType);
                                    Toast.makeText(EditFilmActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
                                    finish(); // Close activity after saving changes
                                } else {
                                    etVideoName.setError("Video name cannot be empty");
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(EditFilmActivity.this, "Video not found", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity if video not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditFilmActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> onBackPressed());

    }
}
