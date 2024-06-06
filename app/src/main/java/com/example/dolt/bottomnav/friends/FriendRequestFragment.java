package com.example.dolt.bottomnav.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dolt.databinding.FragmentFriendRequestBinding;
import com.example.dolt.users.User;
import com.example.dolt.users.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class FriendRequestFragment extends Fragment {

    private FragmentFriendRequestBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendRequestBinding.inflate(inflater, container, false);

        loadFriendRequests();

        return binding.getRoot();
    }

    public void loadFriendRequests(){
        ArrayList<User> users = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String friendRequestsStr;
                if (snapshot.child(FirebaseAuth.getInstance().getUid()).child("intoFriendRequests").getValue() != null) {
                    friendRequestsStr = Objects.requireNonNull(snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("intoFriendRequests").getValue()).toString();

                } else return;
                String[] friendRequestsIds = friendRequestsStr.split(",");
                for (int i = 0; i < friendRequestsIds.length; i++) {
                    String str = friendRequestsIds[i];
                    str = str.substring(1, friendRequestsIds[i].length()-1);
                    if(i==0)
                        str = str.substring(0, friendRequestsIds[i].length()-2);
                    if (i==friendRequestsIds.length-1)
                        str = str.substring(0, friendRequestsIds[i].length()-3);
                    friendRequestsIds[i] = str;
                }

                for (String friendRequestsId : friendRequestsIds){
                    DataSnapshot userSnapshot = snapshot.child(friendRequestsId);
                    String username = Objects.requireNonNull(userSnapshot.child("username").getValue()).toString();

                    User user = new User(username, friendRequestsId, false, true);
                    users.add(user);

                }

                binding.friendRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.friendRequestsRecyclerView.setAdapter(new UsersAdapter(users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
