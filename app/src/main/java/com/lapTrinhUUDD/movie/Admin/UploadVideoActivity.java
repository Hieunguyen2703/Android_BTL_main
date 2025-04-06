package com.lapTrinhUUDD.movie.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lapTrinhUUDD.movie.Models.VideoUploadDetails;
import com.lapTrinhUUDD.movie.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UploadVideoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Uri videoUri;
    private TextView txtViedeoSelected;
    private EditText edtMoviesDescription, edtMoviesName;
    private String videoCategory;
    private String videoSlide  = "";
    private DatabaseReference databaseRef;
    private AlertDialog progressDialog;
    private GoogleDriveUploader googleDriveUploader;
    private GoogleSignInClient googleSignInClient;
    private Button btnUploadVideo;
    private Button btnAddVideo;
    private RadioGroup radioGroupType;
    private RadioButton radioButtonSlide, radioButtonNoSlide;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                } else {
                    Snackbar.make(findViewById(R.id.main), "Google Sign In failed", Snackbar.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> videoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    videoUri = result.getData().getData();
                    txtViedeoSelected.setText("Video Selected(Click to clear)");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        // Initialize views
        txtViedeoSelected = findViewById(R.id.txtVideoSelected);
        edtMoviesDescription = findViewById(R.id.edtMovieDescription);
        edtMoviesName = findViewById(R.id.edtMoviesName);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);
        btnAddVideo = findViewById(R.id.btnAddVideo);

        // Initialize RadioGroup and RadioButtons
        radioGroupType = findViewById(R.id.radioGroupSlide);
        radioButtonSlide = findViewById(R.id.radioButtonSlide);
        radioButtonNoSlide = findViewById(R.id.radioButtonNoSlide);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("videos");

        // Initialize Google Drive Uploader
        googleDriveUploader = new GoogleDriveUploader(this);
        googleSignInClient = googleDriveUploader.getGoogleSignInClient();

        // Setup Spinner
        Spinner spinner = findViewById(R.id.spinnerCategory);
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("Action");
        categories.add("Adventure");
        categories.add("Sport");
        categories.add("Romantic");
        categories.add("Comedy");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        // Setup Progress Dialog
        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.dialog_progress) // Bạn có thể tạo một layout riêng cho hộp thoại này
                .setMessage("Uploading video...")
                .create();

        // Set click listeners
        btnUploadVideo.setOnClickListener(this::openvideoFiles);
        btnAddVideo.setOnClickListener(this::uploadFileToFirebase);
        txtViedeoSelected.setOnClickListener(this::clearVideo);

        // Set RadioGroup listener
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonSlide) {
                videoSlide = "Slide"; // If 'Slide' is selected
            } else {
                videoSlide = "No Slide"; // If 'Normal' is selected
            }
        });

    }

    public void openvideoFiles(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }

    public void uploadFileToFirebase(View view) {
        if (videoUri == null) {
            Snackbar.make(view, "Please select a video", Snackbar.LENGTH_LONG).show();
            return;
        }

        String description = edtMoviesDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Snackbar.make(view, "Please enter a description", Snackbar.LENGTH_LONG).show();
            return;
        }
        String name = edtMoviesName.getText().toString().trim();
        if (name.isEmpty()) {
            Snackbar.make(view, "Please enter a name", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Check if already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Ensure the Google Drive upload uses your Google account
            googleDriveUploader.initializeDriveService(account);
            uploadVideoToDrive(view);
        } else {
            // If not signed in, launch the sign-in process
            signInLauncher.launch(googleSignInClient.getSignInIntent());
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            googleDriveUploader.initializeDriveService(account);
            uploadVideoToDrive(findViewById(R.id.main));
        } catch (ApiException e) {
            Snackbar.make(findViewById(R.id.main), "Google Sign In Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void uploadVideoToDrive(View view) {
        progressDialog.show();

        String videoName = "video_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Use your Google account to upload the video to your Google Drive
        googleDriveUploader.uploadVideoToDrive(videoUri, videoName)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful() && task.getResult() != null) {
                        String driveUrl = task.getResult();
                        saveVideoDetailsToFirebase(driveUrl, view);
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Snackbar.make(view, "Upload failed: " + error, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void saveVideoDetailsToFirebase(String videoUrl, View view) {
        String description = edtMoviesDescription.getText().toString().trim();
        String name = edtMoviesName.getText().toString().trim();


        VideoUploadDetails videoDetails = new VideoUploadDetails(
                videoSlide, // video_slide
                "", // video_type (normal or slide)
                "", // video_thumb
                videoUrl,
                name,
                description,
                videoCategory
        );

        String uploadId = databaseRef.push().getKey();
        if (uploadId != null) {
            databaseRef.child(uploadId).setValue(videoDetails)
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(view, "Video uploaded successfully!", Snackbar.LENGTH_LONG).show();
                        // Chuyển sang UploadThumbnailActivity và truyền dữ liệu video
                        Intent intent = new Intent(UploadVideoActivity.this, UploadThumbnailActivity.class);
                        intent.putExtra("videoId", uploadId);
                        intent.putExtra("videoUrl", videoUrl);
                        startActivity(intent);
                        txtViedeoSelected.setText("No Video Selected");
                        edtMoviesDescription.setText("");
                        videoUri = null;
                    })
                    .addOnFailureListener(e ->
                            Snackbar.make(view, "Failed to save video details: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        videoCategory = parent.getItemAtPosition(position).toString();
        Toast.makeText(this, "Selected " + videoCategory, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void clearVideo(View view) {
        videoUri = null;
        txtViedeoSelected.setText("No video selected");
    }
}
