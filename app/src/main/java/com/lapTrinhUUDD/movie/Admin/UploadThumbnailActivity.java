package com.lapTrinhUUDD.movie.Admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lapTrinhUUDD.movie.R;
import java.util.HashMap;
import java.util.Map;

public class UploadThumbnailActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView txtVideoSelected;
    private Button btnUploadThumbnail, buttonUpload;
    private RadioGroup radioGroup;
    private Uri imageUri;
    private DatabaseReference databaseRef;
    private AlertDialog progressDialog;
    private GoogleDriveUploader googleDriveUploader;
    private String videoId;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imageView.setImageURI(imageUri);
                    txtVideoSelected.setText("Image Selected (Click to Clear)");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_thumbnail);

        imageView = findViewById(R.id.imageView);
        txtVideoSelected = findViewById(R.id.txtVideoSelected);
        btnUploadThumbnail = findViewById(R.id.btnUploadThumbnail);
        buttonUpload = findViewById(R.id.buttonUpload);
        radioGroup = findViewById(R.id.radioGroup);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        videoId = getIntent().getStringExtra("videoId");
        googleDriveUploader = new GoogleDriveUploader(this);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            googleDriveUploader.initializeDriveService(account);
        }

        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.dialog_progress)
                .setMessage("Uploading thumbnail...")
                .create();

        btnUploadThumbnail.setOnClickListener(this::showImageChooser);
        buttonUpload.setOnClickListener(this::uploadThumbnailToFirebase);
        txtVideoSelected.setOnClickListener(this::clearImage);
    }

    private void showImageChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadThumbnailToFirebase(View view) {
        if (imageUri == null) {
            Snackbar.make(view, "Please select an image", Snackbar.LENGTH_LONG).show();
            return;
        }
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Snackbar.make(view, "Please select a movie type", Snackbar.LENGTH_LONG).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String movieType = selectedRadioButton.getText().toString();
        progressDialog.show();
        String fileName = "thumbnail_" + System.currentTimeMillis();

        googleDriveUploader.uploadImageToDrive(imageUri, fileName)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful() && task.getResult() != null) {
                        String imageUrl = task.getResult();
                        saveThumbnailData(imageUrl, movieType);
                    } else {
                        Snackbar.make(view, "Upload failed: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void saveThumbnailData(String imageUrl, String movieType) {
        // Lấy thông tin video từ database
        databaseRef.child("videos").child(videoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Map chứa các dữ liệu cần cập nhật
                    Map<String, Object> updatedData = new HashMap<>();
                    updatedData.put("video_thumb", imageUrl);  // Cập nhật ảnh thumbnail
                    updatedData.put("video_type", movieType);  // Cập nhật loại video

                    // Lấy tất cả các thuộc tính cũ của video và thêm vào bản cập nhật
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String key = child.getKey();
                        if (!updatedData.containsKey(key)) {
                            updatedData.put(key, child.getValue());  // Giữ nguyên các thuộc tính cũ
                        }
                    }

                    // Cập nhật video với các dữ liệu mới
                    databaseRef.child("videos").child(videoId).updateChildren(updatedData)
                            .addOnSuccessListener(aVoid -> {
                                Snackbar.make(findViewById(R.id.main), "Thumbnail uploaded successfully!", Snackbar.LENGTH_LONG).show();
                                clearImage(null);  // Xóa ảnh đã chọn
                                finish();  // Hoàn thành và thoát Activity
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(findViewById(R.id.main), "Failed to update video: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Snackbar.make(findViewById(R.id.main), "Error fetching video: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }




    public void clearImage(View view) {
        imageUri = null;
        imageView.setImageResource(R.drawable.ic_launcher_background);
        txtVideoSelected.setText("No image selected ");
    }
}