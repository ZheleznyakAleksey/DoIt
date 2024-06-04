package com.example.dolt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dolt.bottomnav.friends.FriendsFragment;
import com.example.dolt.bottomnav.profile.ProfileFragment;
import com.example.dolt.bottomnav.tasks.TasksFragment;
import com.example.dolt.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();

        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        int startFragmentId = R.id.tasks;
        Fragment startFragment = new TasksFragment();
        if (extras != null) {
            if (Objects.equals(extras.getString("fragment"), "friendsFragment")) {
                startFragment = new FriendsFragment();
                startFragmentId = R.id.friends;
            }
            else if (Objects.equals(extras.getString("fragment"), "profileFragment")) {
                startFragment = new ProfileFragment();
                startFragmentId = R.id.profile;
            } else if (Objects.equals(extras.getString("fragment"), "tasksFragment")) {
                startFragment = new TasksFragment();
                startFragmentId = R.id.tasks;
            }
        }

        getSupportFragmentManager().beginTransaction().replace(binding.fragmentContainer.getId(), startFragment).commit();
        binding.bottomNav.setSelectedItemId(startFragmentId);

        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.tasks, new TasksFragment());
        fragmentMap.put(R.id.friends, new FriendsFragment());
        fragmentMap.put(R.id.profile, new ProfileFragment());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = fragmentMap.get(item.getItemId());

            assert fragment != null;
            getSupportFragmentManager().beginTransaction().replace(binding.fragmentContainer.getId(), fragment).commit();

            return true;
        });

        getFCMToken();
        }

        void getFCMToken() {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    HashMap<String, Object> htoken = new HashMap<>();
                    htoken.put("fcmToken", token);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).updateChildren(htoken);
                }
            });
        }

        public static void makeToast(Context context, String text) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
}