package com.example.dolt;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dolt.users.User;
import com.example.dolt.utils.DatabaseFriends;
import com.google.firebase.auth.FirebaseAuth;

public class DifferentMethods {

    public static User getCurrentUser(Context context) {
        DatabaseFriends databaseFriends;
        databaseFriends = new DatabaseFriends(context);
        databaseFriends.openDatabase();
        User currentUser = databaseFriends.getFriendByUserId(FirebaseAuth.getInstance().getUid());
        databaseFriends.close();
        return currentUser;
    }

    public static boolean CheckInternet(@NonNull Context context)
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        assert wifi != null;
        if (wifi.isConnected()) {
            return true;
        } else {
            assert mobile != null;
            return mobile.isConnected();
        }
    }

    public static void makeToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void intentToNoInternetConnectionActivity(Context context) {
        Intent intent = new Intent(context, NoInternetConnection.class);

        context.startActivity(intent);
    }


}
