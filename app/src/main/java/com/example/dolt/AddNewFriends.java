package com.example.dolt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dolt.users.User;
import com.example.dolt.users.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class AddNewFriends extends AppCompatActivity {
    com.example.dolt.databinding.ActivityAddNewFriendsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = com.example.dolt.databinding.ActivityAddNewFriendsBinding.inflate(getLayoutInflater());

        loadUsers();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(AddNewFriends.this, MainActivity.class);
                   intent.putExtra("id", R.id.friends);
                   intent.putExtra("fragment", "friendsFragment");
                   startActivity(intent);
               }
           }
        );

        setContentView(binding.getRoot());

    }

    public void loadUsers(){
        ArrayList<User> users = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String usersStr = Objects.requireNonNull(snapshot.child("Users").child("users").getValue()).toString();
                String[] usersIds = usersStr.split(",");
                if (usersIds[0].length()<4) return;
                for (int i = 0; i < usersIds.length; i++) {
                    String str = usersIds[i];
                    str = str.substring(1, usersIds[i].length()-1);
                    if(i==0)
                        str = str.substring(0, usersIds[i].length()-2);
                    if (i==usersIds.length-1)
                        str = str.substring(0, usersIds[i].length()-3);
                    usersIds[i] = str;
                }

                String friendsStr = Objects.requireNonNull(snapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").getValue()).toString();
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

                ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < usersIds.length; i++) {
                    for (int j = 0; j < friendsIds.length; j++) {
                        if(Objects.equals(usersIds[i], friendsIds[j]))
                            indexes.add(i);
                    }
                }
                Collections.reverse(indexes);
                for (int index : indexes) {
                    usersIds = remove(usersIds, index);

                }

                if (usersIds.length==0) return;
                for (String userId : usersIds){
                    DataSnapshot userSnapshot = snapshot.child("Users").child(userId);
                    String username = Objects.requireNonNull(userSnapshot.child("username").getValue()).toString();
                    //String profileImage = snapshot.child("profileImage").getValue().toString();

                    User user = new User(username, userId, false);
                    users.add(user);
                }

                binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                binding.usersRecyclerView.setAdapter(new UsersAdapter(users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static String[] remove(String[] values, int index) {
        // Создаем пустой массив размером на один меньше чем исходный
        // так как мы удаляем один элемент
        String[] result = new String[values.length - 1];

        for (int i = 0; i < values.length; i++) {
            if (i != index) { // Копируем все кроме index
                // Элементы стоящие дальше index смещаются влево
                int newIndex = i < index ? i : i - 1;
                result[newIndex] = values[i];
            }
        }

        return result;
    }
}
