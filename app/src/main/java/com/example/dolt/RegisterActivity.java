package com.example.dolt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dolt.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUpBtn.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String newusername = binding.usernameEt.getText().toString();
                    boolean alreadyThereIs = false;
                    String usersStr = Objects.requireNonNull(snapshot.child("Users").child("users").getValue()).toString();
                    String[] usersIds = usersStr.split(",");
                    if (usersIds[0].length() < 4) return;
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

                    if (binding.emailEt.getText().toString().isEmpty() || binding.passwordEt.getText().toString().isEmpty()
                            || binding.usernameEt.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(), "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show();
                    } else if (binding.usernameEt.getText().length() > 20) {
                        Toast.makeText(getApplicationContext(), "Имя пользователя не может быть длинее 20 символов", Toast.LENGTH_SHORT).show();
                    } else if (alreadyThereIs) {
                        MainActivity.makeToast(getApplicationContext(), "Данное имя пользователя уже занято");
                    } else{
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        HashMap<String, String> userInfo = new HashMap<>();
                                        userInfo.put("email", binding.emailEt.getText().toString());
                                        userInfo.put("username", binding.usernameEt.getText().toString());
                                        userInfo.put("profileImage", "");

                                        FirebaseDatabase.getInstance().getReference(/*"https://doit1-f5cf4-default-rtdb.europe-west1.firebasedatabase.app"*/).child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                .setValue(userInfo);
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("friends")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue("");
                                        FirebaseDatabase.getInstance().getReference().child("Users").child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("");

                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Пожалуйста, попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        binding.backBtn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

    }
}