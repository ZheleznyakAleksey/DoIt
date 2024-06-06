package com.example.dolt.bottomnav.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dolt.AddNewFriends;
import com.example.dolt.R;
import com.example.dolt.databinding.FragmentFriendsBinding;

import java.util.HashMap;
import java.util.Map;

public class FriendsFragment extends Fragment {
    private FragmentFriendsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);

        getParentFragmentManager().beginTransaction().replace(binding.fragmentContainer2.getId(), new MyFriendsFragment()).commit();
        binding.friendsNav.setSelectedItemId(R.id.myFriends);

        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.myFriends, new MyFriendsFragment());
        fragmentMap.put(R.id.friendRequests, new FriendRequestFragment());

        binding.friendsNav.setOnItemSelectedListener(item -> {
            Fragment fragment = fragmentMap.get(item.getItemId());

            assert fragment != null;
            getParentFragmentManager().beginTransaction().replace(binding.fragmentContainer2.getId(), fragment).commit();

            return true;
        });

        binding.searchNewFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(FriendsFragment.this.getContext(), AddNewFriends.class);
                startActivity(i);
            }
        });

        return binding.getRoot();
    }


}