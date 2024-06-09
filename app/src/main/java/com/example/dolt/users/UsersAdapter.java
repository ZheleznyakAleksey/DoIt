package com.example.dolt.users;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dolt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {
    private ArrayList<User> users;

    public UsersAdapter(ArrayList<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, parent, false);
        return new UserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.username.setText(users.get(position).getUsername());
        if (getItem(position).getFriendRequest()) {
            holder.acceptRejectFriendRequest.setVisibility(View.VISIBLE);
            holder.addNewFriend.setVisibility(View.GONE);
            holder.deleteFriend.setVisibility(View.GONE);
        } else {
            holder.acceptRejectFriendRequest.setVisibility(View.GONE);
            holder.addNewFriend.setVisibility(View.VISIBLE);
            holder.deleteFriend.setVisibility(View.VISIBLE);
        }
        if (users.get(position).getFriend()) {
            holder.addNewFriend.setVisibility(View.GONE);
            holder.deleteFriend.setColorFilter(Color.BLACK);
            if (users.get(position).getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.deleteFriend.setVisibility(View.GONE);
                holder.username.setText(users.get(position).getUsername() + " (Я)");
            } else {
                holder.deleteFriend.setVisibility(View.VISIBLE);
                holder.username.setText(users.get(position).getUsername());
            }
        } else {
            holder.deleteFriend.setVisibility(View.GONE);
            holder.addNewFriend.setColorFilter(Color.WHITE);
        }

        FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId())
                .child("profileImage").get()
                .addOnCompleteListener(task -> {
                    try{
                        String profileImageUrl = task.getResult().getValue().toString();

                        if (!profileImageUrl.isEmpty())
                            Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.profileImage);
                    }catch(Exception e){
                        Toast.makeText(holder.itemView.getContext(), "Failed to get profile image link", Toast.LENGTH_SHORT).show();
                    }
                });

        holder.addNewFriend.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("outFriendRequests").child(users.get(position).getUserId()).setValue("");
            FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("intoFriendRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("");
            removeItem(holder);

        });
        holder.deleteFriend.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Удалить из друзей");
            builder.setMessage("Вы уверены, что хотите удалить пользователя '" + users.get(position).getUsername() + "' из друзей?");
            builder.setPositiveButton("Да, уверен",
                    (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(users.get(position).getUserId()).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                        removeItem(holder);
                    });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        holder.acceptRequestFriend.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("friends").child(users.get(position).getUserId()).setValue("");
            FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("");
            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("intoFriendRequests").child(users.get(position).getUserId()).setValue(null);
            FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("outFriendRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
            removeItem(holder);
        });
        holder.rejectRequestFriend.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("intoFriendRequests").child(users.get(position).getUserId()).setValue(null);
            FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("outFriendRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
            removeItem(holder);
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void removeItem(UserViewHolder holder) {
        int actualPosition = holder.getAdapterPosition();
        users.remove(actualPosition);
        notifyItemRemoved(actualPosition);
        notifyItemRangeChanged(actualPosition, users.size());
    }

    public User getItem(int position) {
        return users.get(position);
    }
}
