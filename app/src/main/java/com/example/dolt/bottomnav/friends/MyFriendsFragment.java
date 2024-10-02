package com.example.dolt.bottomnav.friends;

import static com.example.dolt.DifferentMethods.makeToast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dolt.databinding.FragmentMyFriendsBinding;
import com.example.dolt.users.User;
import com.example.dolt.users.UsersAdapter;
import com.example.dolt.utils.DatabaseFriends;

import java.util.ArrayList;

public class MyFriendsFragment extends Fragment {

    private FragmentMyFriendsBinding binding;
    DatabaseFriends databaseFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyFriendsBinding.inflate(inflater, container, false);
        databaseFriends = new DatabaseFriends(MyFriendsFragment.this.getContext());

        loadFriends();

        binding.updateFriendsButton.setOnClickListener(v -> {
            databaseFriends.openDatabase();
            databaseFriends.deleteAllFriends();
            databaseFriends.insertAllFriends(this.getContext());
            databaseFriends.close();
            makeToast(MyFriendsFragment.this.getContext(), "Друзья обновлены");
        });

        return binding.getRoot();
    }

    public void loadFriends(){
        databaseFriends = new DatabaseFriends(MyFriendsFragment.this.getContext());
        databaseFriends.openDatabase();
        ArrayList<User> myFriends = databaseFriends.getAllFriends();
        databaseFriends.close();
        binding.friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.friendsRecyclerView.setAdapter(new UsersAdapter(myFriends));
    }
}
