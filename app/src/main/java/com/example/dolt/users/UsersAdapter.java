package com.example.dolt.users;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dolt.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {
    private ArrayList<User> users = new ArrayList<>();

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
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DataSnapshot> task) {
                        try{
                            String profileImageUrl = task.getResult().getValue().toString();

                            if (!profileImageUrl.isEmpty())
                                Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.profileImage);
                        }catch(Exception e){
                            Toast.makeText(holder.itemView.getContext(), "Failed to get profile image link", Toast.LENGTH_SHORT).show();
                        }
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
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(users.get(position).getUserId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                            removeItem(holder);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        holder.acceptRequestFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(users.get(position).getUserId()).setValue("");
                FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("");
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("outFriendRequests").child(users.get(position).getUserId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("intoFriendRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                removeItem(holder);
            }
        });
        holder.rejectRequestFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("outFriendRequests").child(users.get(position).getUserId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Users").child(users.get(position).getUserId()).child("intoFriendRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                removeItem(holder);
            }
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
