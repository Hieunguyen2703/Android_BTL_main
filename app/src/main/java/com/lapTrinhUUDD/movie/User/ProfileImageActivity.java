package com.lapTrinhUUDD.movie.User;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.lapTrinhUUDD.movie.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileImageActivity extends AppCompatActivity {
    private ImageView backButton, logoutBtn, userImage;
    private TextView title;
    private Button chooseImageBtn, uploadImageBtn, deleteImageBtn;
    private String imageUrl, imageUriString;
    StorageReference mStoragerefUserImage, imageRef;
    DatabaseReference updateimageref;
    StorageTask mStorageTask;
    FirebaseUser user;
    Uri imageUri;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        iniActionBar();
        chooseImageBtn = findViewById(R.id.image_choose);
        uploadImageBtn = findViewById(R.id.image_upload);
        mStoragerefUserImage = FirebaseStorage.getInstance().getReference().child("UserImage");
        user = FirebaseAuth.getInstance().getCurrentUser();
        updateimageref = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        userImage = findViewById(R.id.user_image);
        deleteImageBtn = findViewById(R.id.image_delete);

        imageUriString = getIntent().getExtras().getString("imageUri");
        if (!imageUriString.equals("")){
            imageUri = Uri.parse(imageUriString);
            Glide.with(this).load(imageUri).into(userImage);
            deleteImageBtn.setVisibility(View.VISIBLE);
        }else{
            deleteImageBtn.setVisibility(View.GONE);

        }

        deleteImageBtn.setOnClickListener(v -> {
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Image")
                    .setMessage("Are you sure, you want to delete this image?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, which) -> {deleteImage();})
                    .setNegativeButton("No", (dialog, which) -> {dialog.cancel();})
                    .show();
        });
    }

    private void deleteImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("wait deleting image ...");
        progressDialog.show();
        imageRef  = mStoragerefUserImage.child(user.getUid());
        updateimageref.child("img").setValue("").addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                imageRef.delete().addOnSuccessListener(aVoid ->{
                    progressDialog.dismiss();
                    userImage.setImageResource(R.drawable.icon_user);
                    deleteImageBtn.setVisibility(View.GONE);
                    Toast.makeText(this, "Image Deleted", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image Deleted Failed", Toast.LENGTH_SHORT).show();
                });
            }else{
                progressDialog.dismiss();
                Toast.makeText(this, "Image Deleted Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar_profile);

            title = findViewById(R.id.title);
            title.setText("Profile Image");

            backButton = findViewById(R.id.btn_back);
            backButton.setOnClickListener(v -> startActivity(new Intent(ProfileImageActivity.this, UserProfileActivity.class)));

            logoutBtn = findViewById(R.id.btn_logout);
            logoutBtn.setVisibility(View.GONE);
        }
    }

    public void chooseImage(View view) {
        Intent in = new Intent(Intent.ACTION_GET_CONTENT);
        in.setType("image/*");
        startActivityForResult(in,102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                userImage.setImageBitmap(bitmap);

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void uploadimagetofirebase(View view){
        if (imageUri == null){
            Toast.makeText(this, "first select an image", Toast.LENGTH_SHORT).show();
        }else {
            if (mStorageTask != null && mStorageTask.isInProgress()){
                Toast.makeText(this, "upload files already in progress", Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }
    }
    private void uploadImage(){
        if (imageUri != null){
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("wait uploading image ...");
            progressDialog.show();

            final StorageReference sRef = mStoragerefUserImage.child(user.getUid());

            sRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl = uri.toString();
                            updateimageref.child("img").setValue(imageUrl);
                            progressDialog.dismiss();
                            Toast.makeText(ProfileImageActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileImageActivity.this, UserProfileActivity.class));
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileImageActivity.this, "Image Uploaded Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}