package com.lapTrinhUUDD.movie.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lapTrinhUUDD.movie.Admin.EditAccountActivity;
import com.lapTrinhUUDD.movie.Models.User;
import com.lapTrinhUUDD.movie.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.UserViewHolder> {
    private Context mContext;
    private List<User> mUserList;

    public AccountAdapter(Context context, List<User> userList) {
        mContext = context;
        mUserList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.account_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = mUserList.get(position);
        holder.tvUsername.setText("Username: " + currentUser.getUsername());
        holder.tvEmail.setText("Email: " + currentUser.getEmail());
        holder.tvDob.setText("Date of Birth: " + currentUser.getDob());
        holder.tvGender.setText("Gender: " + currentUser.getGender());

        holder.btnEdit.setOnClickListener(v -> {
            // Start EditUserActivity to edit user details
            Intent intent = new Intent(mContext, EditAccountActivity.class);
            intent.putExtra("username", currentUser.getUsername());
            intent.putExtra("email", currentUser.getEmail());
            intent.putExtra("dob", currentUser.getDob());
            intent.putExtra("gender", currentUser.getGender());
            mContext.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Confirm delete action with a dialog
            new AlertDialog.Builder(mContext)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete user from Firebase
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUsername());
                        dbRef.removeValue().addOnSuccessListener(aVoid -> {
                            mUserList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, mUserList.size());
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public TextView tvEmail;
        public TextView tvDob;
        public TextView tvGender;
        public Button btnEdit;
        public Button btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvDob = itemView.findViewById(R.id.tvDob);
            tvGender = itemView.findViewById(R.id.tvGender);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
