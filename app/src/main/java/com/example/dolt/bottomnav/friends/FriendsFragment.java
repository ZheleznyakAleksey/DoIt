package com.example.dolt.bottomnav.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dolt.AddNewFriends;
import com.example.dolt.databinding.FragmentFriendsBinding;
import com.example.dolt.users.User;
import com.example.dolt.users.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class FriendsFragment extends Fragment {
    private FragmentFriendsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);

        loadFriends();

        binding.searchNewFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(FriendsFragment.this.getContext(), AddNewFriends.class);
                startActivity(i);
            }
        });

        return binding.getRoot();
    }

    public void loadFriends(){
        ArrayList<User> users = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String friendsStr = Objects.requireNonNull(snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").getValue()).toString();
                String[] friendsIds = friendsStr.split(",");
                for (int i = 0; i < friendsIds.length; i++) {
                    String str = friendsIds[i];
                    str = str.substring(1, friendsIds[i].length()-1);
                    if(i==0)
                        str = str.substring(0, friendsIds[i].length()-2);
                    if (i==friendsIds.length-1)
                        str = str.substring(0, friendsIds[i].length()-3);
                    friendsIds[i] = str;
                }

                for (String friendId : friendsIds){
                    DataSnapshot userSnapshot = snapshot.child(friendId);
                    String username = Objects.requireNonNull(userSnapshot.child("username").getValue()).toString();

                    User user = new User(username, friendId, true);
                    users.add(user);

                }

                binding.friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.friendsRecyclerView.setAdapter(new UsersAdapter(users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}