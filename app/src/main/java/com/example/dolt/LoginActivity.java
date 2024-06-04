package com.example.dolt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dolt.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginBtn.setOnClickListener(v -> {
            if (binding.emailEt.getText().toString().isEmpty() || binding.passwordEt.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Поля не могут быль пустыми", Toast.LENGTH_SHORT).show();
            }else{
                FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                                if (getIntent().getExtras()!=null) {
                                    intent.putExtra("fragment", getIntent().getExtras().getString("fragment"));
                                }
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Неправильно введена электронная почта или пароль", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });

        binding.goToRegisterActivityTv.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }


}