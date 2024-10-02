package com.example.dolt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    // 1. Скачивание изображения по URL и получение пути к нему
    public static String downloadImage(String userId) {
        // Получаем ссылку на изображение из Firebase Database
        String imageUrl = "https://amimore.ru/img/bi/kapibara-brelok-1688472205.jpg";//getImageURLFromFirebase(userId);

        if (imageUrl != null) {
            try {
                URL url = new URL(imageUrl);
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (bitmap != null) {
                    // Сохраняем изображение на телефон и получаем путь
                    String imagePath = saveImageToStorage(bitmap);
                    return imagePath;
                } else {
                    Log.e(TAG, "Не удалось получить изображение из URL: " + imageUrl);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при скачивании изображения: " + e.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "Ссылка на изображение не найдена в Firebase Database.");
            return null;
        }
    }

    // Функция для получения ссылки на изображение из Firebase Database
    public static String getImageURLFromFirebase(String userId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] imageUrl = {null};

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("profileImage");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageUrl[0] = dataSnapshot.getValue(String.class);
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Ошибка при чтении данных из Firebase Database: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "Ошибка ожидания: " + e.getMessage());
        }

        return imageUrl[0];
    }

    // Вспомогательная функция для сохранения изображения на телефон
    private static String saveImageToStorage(Bitmap bitmap) {
        try {
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile("profile", ".jpg", storageDir);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d(TAG, "Изображение успешно сохранено: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении изображения: " + e.getMessage());
            return null;
        }
    }

    public static void setImageToCircleImageView(String imagePath, CircleImageView circleImageView) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                circleImageView.setImageBitmap(bitmap);
            } else {
                // Обработка ошибки: изображение не найдено
                Log.e(TAG, "Изображение не найдено по пути: " + imagePath);
                // Например, можно установить изображение по умолчанию
                // circleImageView.setImageResource(R.drawable.default_image);
            }
        } catch (OutOfMemoryError e) {
            // Обработка ошибки: нехватка памяти
            Log.e(TAG, "Ошибка: нехватка памяти при декодировании изображения.", e);
            // Например, можно попробовать декодировать изображение с меньшим размером
            // или установить изображение по умолчанию
            // circleImageView.setImageResource(R.drawable.default_image);
        } catch (Exception e) {
            // Обработка других ошибок
            Log.e(TAG, "Ошибка при установке изображения: " + e.getMessage(), e);
            // Например, можно установить изображение по умолчанию
            // circleImageView.setImageResource(R.drawable.default_image);
        }
    }
}
