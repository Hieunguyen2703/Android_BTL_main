package com.lapTrinhUUDD.movie.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lapTrinhUUDD.movie.Admin.EditFilmActivity;
import com.lapTrinhUUDD.movie.Models.GetVideoDetails;
import com.lapTrinhUUDD.movie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GetMovieForAdminHomeAdapter extends RecyclerView.Adapter<GetMovieForAdminHomeAdapter.VideoViewHolder> {
    private Context mContext;
    private List<GetVideoDetails> mVideoList;

    public GetMovieForAdminHomeAdapter(Context context, List<GetVideoDetails> videoList) {
        mContext = context;
        mVideoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        GetVideoDetails currentVideo = mVideoList.get(position);
        holder.tvVideoName.setText(currentVideo.getVideo_name());
        holder.tvVideoDescription.setText(currentVideo.getVideo_description());
        holder.tvVideoCategory.setText(currentVideo.getVideo_category());
        holder.tvVideoType.setText(currentVideo.getVideo_type());

        holder.btnEdit.setOnClickListener(v -> {
            // Code to edit the video
            Intent intent = new Intent(mContext, EditFilmActivity.class);
            intent.putExtra("video_name", currentVideo.getVideo_name()); // Pass video_name to EditFilmActivity
            mContext.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(currentVideo.getVideo_name());
        });
    }
    private void showDeleteConfirmationDialog(String video_name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this video?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteVideo(video_name);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteVideo(String video_name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");

        // Query to find the video by video_name
        databaseReference.orderByChild("video_name").equalTo(video_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(mContext, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                        // Optionally, update the UI to reflect the removal of the item
                        mVideoList.remove(snapshot.getValue(GetVideoDetails.class));
                        notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(mContext, "Failed to delete video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Failed to delete video: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        public TextView tvVideoName, tvVideoDescription, tvVideoCategory, tvVideoType;
        public Button btnEdit;
        public Button btnDelete;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvVideoName = itemView.findViewById(R.id.tvVideoName);
            tvVideoDescription = itemView.findViewById(R.id.tvVideoDescription);
            tvVideoCategory = itemView.findViewById(R.id.tvVideoCategory);
            tvVideoType = itemView.findViewById(R.id.tvVideoType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
