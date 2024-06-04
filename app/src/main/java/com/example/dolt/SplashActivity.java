package com.example.dolt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //getSupportActionBar().hide();

        if (getIntent().getExtras()!=null){
            if (getIntent().getExtras().getString("fragment") != null) {
                if (FirebaseAuth.getInstance().getCurrentUser()==null) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("fragment", getIntent().getExtras().getString("fragment"));
                    startActivity(intent);
                } else {
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainIntent);

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("fragment", getIntent().getExtras().getString("fragment"));
                    startActivity(intent);
                    finish();
                }
            }
            //String userId = getIntent().getExtras().getString("userId");
            //FirebaseDatabase.getInstance().getReference().child("Users").child(userId).get()
            //        .addOnCompleteListener(task -> {
            //            if (task.isSuccessful()) {
            //                Toast.makeText(getApplicationContext(), task.getResult().getValue().toString(), Toast.LENGTH_SHORT).show();
            //
            //            }
            //        });

        }
        if (getIntent().getExtras()==null || (getIntent().getExtras()!=null && getIntent().getExtras().getString("fragment")==null)) {
            if(FirebaseAuth.getInstance().getCurrentUser()==null) {
                final Intent i = new Intent(SplashActivity.this, LoginActivity.class);

                new Handler().postDelayed(() -> {
                    startActivity(i);
                    finish();
                }, 1000);
            } else {
                final Intent i = new Intent(SplashActivity.this, MainActivity.class);

                new Handler().postDelayed(() -> {
                    startActivity(i);
                    finish();
                }, 1000);
            }
        }

    }
}