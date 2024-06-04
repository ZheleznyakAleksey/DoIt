package com.example.dolt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dolt.databinding.ActivitySendingForVerificationBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;

public class SendingForVerification extends AppCompatActivity {

    private ActivitySendingForVerificationBinding binding;
    private Uri filePath;
    private boolean isSelectedImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendingForVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String taskId = extras.getString("taskId");
        String taskText = extras.getString("taskText");
        String userFrom = extras.getString("userFrom");
        String degreeOfImportance = String.valueOf(extras.getInt("degreeOfImportance"));
        String termDateTime = extras.getString("termDateTime");
        int taskCheck = extras.getInt("taskCheck");
        String degreeOfImportanceText;
        if (Integer.parseInt(degreeOfImportance)==0) {
            degreeOfImportanceText = "Неважно";
        } else if (Integer.parseInt(degreeOfImportance)==1) {
            degreeOfImportanceText = "Не очень важно";
        } else if (Integer.parseInt(degreeOfImportance)==2) {
            degreeOfImportanceText = "Важно";
        } else if (Integer.parseInt(degreeOfImportance)==3) {
            degreeOfImportanceText = "Очень важно";
        } else {
            degreeOfImportanceText = "Неизвестная важность задачи";
        }
        binding.taskText.setText(taskText);
        binding.userFromText.setText(userFrom);
        binding.degreeOfImportance.setText(degreeOfImportanceText);
        binding.termDateTime.setText(termDateTime);

        if (taskCheck==1) {
            binding.imageForChecking.setVisibility(View.GONE);
            binding.textAddImage.setVisibility(View.GONE);
            binding.textForChecking.setVisibility(View.VISIBLE);
        } else if (taskCheck==2) {
            binding.imageForChecking.setVisibility(View.VISIBLE);
            binding.textAddImage.setVisibility(View.VISIBLE);
            binding.textForChecking.setVisibility(View.GONE);
        } else if (taskCheck==3) {
            binding.imageForChecking.setVisibility(View.VISIBLE);
            binding.textAddImage.setVisibility(View.VISIBLE);
            binding.textForChecking.setVisibility(View.VISIBLE);
        }

        binding.imageForChecking.setOnClickListener(v -> selectImage());
        binding.cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(SendingForVerification.this, MainActivity.class);
            startActivity(intent);
        });
        binding.sendingForVerificationButton.setOnClickListener(v -> {
            if (taskCheck==1) {
                if (binding.textForChecking.getText().length()==0) {
                    MainActivity.makeToast(getApplicationContext(), "Пожалуйста, опишите выполнение задания");
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("taskStatus").setValue(2);
                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("textForChecking").setValue(binding.textForChecking.getText().toString());
                    Intent intent = new Intent(SendingForVerification.this, MainActivity.class);
                    startActivity(intent);
                }
            } else if (taskCheck==2) {
                if (isSelectedImage) {
                    uploadImage(taskId);
                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("taskStatus").setValue(2);
                    Intent intent = new Intent(SendingForVerification.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    MainActivity.makeToast(getApplicationContext(), "Пожалуйста, выберете изображение");
                }
            } else if (taskCheck==3) {
                if (binding.textForChecking.getText().length()==0) {
                    MainActivity.makeToast(getApplicationContext(), "Пожалуйста, опишите выполнение задания");
                } else if (isSelectedImage) {
                    uploadImage(taskId);
                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("taskStatus").setValue(2);
                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("textForChecking").setValue(binding.textForChecking.getText().toString());
                    Intent intent = new Intent(SendingForVerification.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    MainActivity.makeToast(getApplicationContext(), "Пожалуйста, выберете изображение");
                }
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK && result.getData()!=null && result.getData().getData()!=null){
                        filePath = result.getData().getData();

                        try{
                            Bitmap bitmap = MediaStore.Images.Media
                                    .getBitmap(
                                            getContentResolver(),
                                            filePath
                                    );
                            binding.imageForChecking.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            isSelectedImage = true;

                        }catch(IOException e){
                            e.printStackTrace();
                        }

                    }
                }
            }
    );

    private void uploadImage(String taskId){
        if (filePath!=null){

            FirebaseStorage.getInstance().getReference().child("images/"+taskId)
                    .putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getApplicationContext(), "Фото успешно загружено", Toast.LENGTH_SHORT).show();

                        FirebaseStorage.getInstance().getReference().child("images/"+taskId).getDownloadUrl()
                                .addOnSuccessListener(uri -> FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId)
                                        .child("taskImage").setValue(uri.toString()));
                    });
        }
    }
}