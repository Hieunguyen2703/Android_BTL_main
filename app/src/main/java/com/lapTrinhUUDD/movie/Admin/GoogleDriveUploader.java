package com.lapTrinhUUDD.movie.Admin;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoogleDriveUploader {
    private static final String TAG = "GoogleDriveUploader";
    private final Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Drive driveService;

    public GoogleDriveUploader(Context context) {
        this.context = context;
    }

    public void initializeDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_FILE)
        );
        credential.setSelectedAccount(account.getAccount());

        driveService = new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                credential)
                .setApplicationName("Movie App")
                .build();
    }

    public Task<String> uploadVideoToDrive(Uri videoUri, String fileName) {
        return Tasks.call(executor, () -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(videoUri);
                String mimeType = context.getContentResolver().getType(videoUri);

                // File metadata
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                fileMetadata.setMimeType(mimeType);

                // Set the parent folder ID (this is the ID of the folder where the file will be uploaded)
                String folderId = "1kMFqhNQyZ8Ea7I-qf7Zr7V3LyGWjidv7"; // Folder ID from the provided URL
                fileMetadata.setParents(Collections.singletonList(folderId));

                // File content
                InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

                // Upload file
                File file = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                // Set permissions to public
                Permission permission = new Permission()
                        .setType("anyone")
                        .setRole("reader");

                driveService.permissions().create(file.getId(), permission).execute();

                // Return the shareable link
                return "https://drive.google.com/uc?id=" + file.getId();
            } catch (Exception e) {
                Log.e(TAG, "Error uploading to Google Drive", e);
                throw e;
            }
        });
    }
    public Task<String> uploadImageToDrive(Uri imageUri, String fileName) {
        return Tasks.call(executor, () -> {
            try {
                // Lấy InputStream của ảnh từ URI
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                String mimeType = context.getContentResolver().getType(imageUri);

                // Tạo metadata cho tệp ảnh
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                fileMetadata.setMimeType(mimeType);  // Đảm bảo kiểu MIME của ảnh đúng (image/jpeg, image/png, v.v.)

                // ID thư mục Google Drive nơi tệp sẽ được tải lên
                String folderId = "1kMFqhNQyZ8Ea7I-qf7Zr7V3LyGWjidv7"; // ID thư mục của bạn
                fileMetadata.setParents(Collections.singletonList(folderId));

                // Tạo InputStreamContent từ tệp ảnh
                InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

                // Tải ảnh lên Google Drive
                File file = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                // Thiết lập quyền truy cập công khai
                Permission permission = new Permission()
                        .setType("anyone")
                        .setRole("reader");

                driveService.permissions().create(file.getId(), permission).execute();

                // Trả về liên kết chia sẻ công khai
                return "https://drive.google.com/uc?id=" + file.getId();
            } catch (Exception e) {
                Log.e(TAG, "Error uploading to Google Drive", e);
                throw e;
            }
        });
    }
    public Task<Void> deleteFileFromDrive(String fileId) {
        return Tasks.call(executor, () -> {
            try {
                driveService.files().delete(fileId).execute();
                Log.d(TAG, "File deleted successfully: " + fileId);
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Error deleting file from Google Drive", e);
                throw e;
            }
        });
    }



    public GoogleSignInClient getGoogleSignInClient() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        return GoogleSignIn.getClient(context, signInOptions);
    }
}