package com.example.dolt;

import static com.example.dolt.DifferentMethods.CheckInternet;
import static com.example.dolt.DifferentMethods.makeToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dolt.databinding.ActivityNoInternetConnectionBinding;

public class NoInternetConnection extends AppCompatActivity {

    private ActivityNoInternetConnectionBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoInternetConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.updateInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckInternet(NoInternetConnection.this)) {
                    startActivity(new Intent(NoInternetConnection.this, MainActivity.class));
                } else {
                    makeToast(NoInternetConnection.this, "Нет подключения к интернету");
                }
            }
        });

        binding.enableOfflineRefem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}