package com.example.dolt.bottomnav.profile;

import static com.example.dolt.DifferentMethods.makeToast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.dolt.LoginActivity;
import com.example.dolt.databinding.FragmentProfileBinding;
import com.example.dolt.users.User;
import com.example.dolt.utils.DatabaseFriends;
import com.example.dolt.utils.DatabaseTasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;


public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private Uri filePath;
    DatabaseFriends databaseFriends;
    DatabaseTasks databaseTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        databaseFriends = new DatabaseFriends(this.getContext());
        databaseTasks = new DatabaseTasks(this.getContext());

        loadUserInfo();

        binding.profileImageView.setOnClickListener(v -> selectImage());
        binding.updateUsernameButton.setOnClickListener(v -> updateUsername());

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    databaseTasks.openDatabase();
                    databaseFriends.openDatabase();
                    databaseFriends.deleteAllFriends();
                    databaseTasks.deleteAllTasks();
                    databaseTasks.close();
                    databaseFriends.close();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        });

        return binding.getRoot();
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()==Activity.RESULT_OK && result.getData()!=null && result.getData().getData()!=null){
                        filePath = result.getData().getData();

                        try{
                            Bitmap bitmap = MediaStore.Images.Media
                                    .getBitmap(
                                            requireContext().getContentResolver(),
                                            filePath
                                    );
                            binding.profileImageView.setImageBitmap(bitmap);
                        }catch(IOException e){
                            e.printStackTrace();
                        }

                        uploadImage();
                        databaseFriends = new DatabaseFriends(requireContext());
                        databaseFriends.openDatabase();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(databaseFriends.getFriend(0).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String profileImage = snapshot.child("profileImage").getValue(String.class);

                                databaseFriends.updateFriend(0, databaseFriends.getFriend(0).getUsername(), profileImage);
                                databaseFriends.close();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {// Handle the error
                                makeToast(requireContext(), "Ошибка получения изображения профиля: " + error.getMessage());
                            }
                        });

                    }
                }
            }
    );

    private void loadUserInfo() {
        databaseFriends = new DatabaseFriends(this.getContext());
        databaseFriends.openDatabase();
        User user = databaseFriends.getFriend(0); // Assuming you store the current user as the first friend
        databaseFriends.close();

        binding.usernameEt.setText(user.getUsername());
        if (!user.getUserImage().equals("null")) {
            Glide.with(getContext()).load(user.getUserImage()).into(binding.profileImageView);
        }
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if(pickImageActivityResultLauncher !=null)
            pickImageActivityResultLauncher.launch(intent);
    }

    private void uploadImage(){
        if (filePath!=null){
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            FirebaseStorage.getInstance().getReference().child("images/"+uid)
                    .putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getContext(), "Фото успешно загружено", Toast.LENGTH_SHORT).show();

                        FirebaseStorage.getInstance().getReference().child("images/"+uid).getDownloadUrl()
                                .addOnSuccessListener(uri -> FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("profileImage").setValue(uri.toString()));
                    });
        }
    }

    public void updateUsername() {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String newusername = binding.usernameEt.getText().toString();
                boolean alreadyThereIs = false;
                String usersStr = Objects.requireNonNull(snapshot.child("Users").child("users").getValue()).toString();
                String[] usersIds = usersStr.split(",");
                if (usersIds[0].length()<4) return;
                for (int i = 0; i < usersIds.length; i++) {
                    String str = usersIds[i];
                    str = str.substring(1, usersIds[i].length() - 1);
                    if (i == 0)
                        str = str.substring(0, usersIds[i].length() - 2);
                    if (i == usersIds.length - 1)
                        str = str.substring(0, usersIds[i].length() - 3);
                    usersIds[i] = str;
                    if (!str.equals(FirebaseAuth.getInstance().getUid())) {
                        if (newusername.equals(snapshot.child("Users").child(str).child("username").getValue().toString()))
                            alreadyThereIs = true;
                    }
                }

                if (newusername.length() > 20)
                    makeToast(getContext(), "Имя пользователя не может быть длинее 20 символов");
                else if (alreadyThereIs) {
                    makeToast(getContext(), "Данное имя пользователя уже занято");
                } else {
                    HashMap<String, Object> newUsername = new HashMap<>();
                    newUsername.put("username", newusername);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).updateChildren(newUsername);
                    databaseFriends = new DatabaseFriends(requireContext());
                    databaseFriends.openDatabase();
                    databaseFriends.updateFriend(0, newusername, databaseFriends.getFriend(0).getUserImage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}