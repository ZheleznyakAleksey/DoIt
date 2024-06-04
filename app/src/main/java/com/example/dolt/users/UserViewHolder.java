package com.example.dolt.users;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolt.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {
    TextView username;
    CircleImageView profileImage;
    FloatingActionButton addNewFriend, deleteFriend;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.username);
        profileImage = itemView.findViewById(R.id.profile_image);
        addNewFriend = itemView.findViewById(R.id.addNewFriend);
        deleteFriend = itemView.findViewById(R.id.deleteFriend);
    }
}
