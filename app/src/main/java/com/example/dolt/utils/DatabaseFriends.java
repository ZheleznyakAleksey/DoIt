package com.example.dolt.utils;

import static com.example.dolt.DifferentMethods.makeToast;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.dolt.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DatabaseFriends extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String NAME = "friendsDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String USERNAME = "userName";
    private static final String USERID = "userId";
    private static final String USERIMAGE = "userImage";

    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USERNAME + " TEXT, " + USERID + " TEXT, " + USERIMAGE + " TEXT)";

    private SQLiteDatabase db;

    public DatabaseFriends(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        onCreate(db);
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    public void insertFriend(User user, Context context) {
        ContentValues cv = new ContentValues();
        if (user.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            cv.put(ID, 0);
        }
        cv.put(USERNAME, user.getUsername());
        cv.put(USERID, user.getUserId());// Get the profile image URL from Firebase
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileImage = snapshot.child("profileImage").getValue(String.class);
                if (profileImage != null) {
                    cv.put(USERIMAGE, profileImage);
                } else {
                    // Handle the case where the profile image is missing// You might want to set a default image or log an error
                    makeToast(context, "Изображение профиля не найдено для пользователя " + user.getUserId());
                    cv.put(USERIMAGE, "null");
                }
                openDatabase();
                db.insert(TODO_TABLE, null, cv);
                close();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {// Handle the error
                makeToast(context, "Ошибка получения изображения профиля: " + error.getMessage());
            }
        });
    }

    public void insertAllFriends(Context context){
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                insertFriend(new User(snapshot.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("username").getValue(String.class),
                        FirebaseAuth.getInstance().getUid(), true, false), context);

                Object friendsValue = snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").getValue();
                if (friendsValue != null) {
                    String friendsStr = friendsValue.toString();
                    String[] friendsIds = friendsStr.split(",");

                    for (int i = 0; i < friendsIds.length; i++) {
                        String friendId = friendsIds[i].trim();
                        if (i == 0) {
                            friendId = friendId.substring(1, friendId.length() - 1);
                            if (i+1 == friendsIds.length) {
                                friendId = friendId.substring(0, friendId.length() - 1);
                            }
                        }
                        else if (i+1 == friendsIds.length) {
                            friendId = friendId.substring(0, friendId.length() - 2);
                        } else {
                            friendId = friendId.substring(0, friendId.length() - 1);
                        }
                        if (FirebaseAuth.getInstance().getUid().equals(friendId)) {
                            continue;
                        }

                        DataSnapshot userSnapshot = snapshot.child(friendId);
                        String username = Objects.requireNonNull(userSnapshot.child("username").getValue(String.class));

                        User user = new User(username, friendId, true, false);
                        insertFriend(user, context);
                    }
                } else {
                    makeToast(context, "Друзей не существует");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(context, "Не удалось загрузить друзей");
            }
        });
    }

    @SuppressLint("Range")
    public User getFriend(int id) {
        User user = new User();
        try (Cursor cursor = db.query(TODO_TABLE, null, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                user.setUserId(cursor.getString(cursor.getColumnIndex(USERID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(USERNAME)));
                user.setUserImage(cursor.getString(cursor.getColumnIndex(USERIMAGE)));
                user.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            }
        }
        return user;
    }

    @SuppressLint("Range")
    public User getFriendByUserId(String userId) {
        User user = new User();
        try (Cursor cursor = db.query(TODO_TABLE, null, USERID + " = ?", new String[]{userId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                user.setUserId(cursor.getString(cursor.getColumnIndex(USERID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(USERNAME)));
                user.setUserImage(cursor.getString(cursor.getColumnIndex(USERIMAGE)));
                user.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            }
        }
        return user;
    }

    @SuppressLint("Range")
    public ArrayList<User> getAllFriends(){
        ArrayList<User> friendsList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try{
            cur = db.query(TODO_TABLE, null, null, null, null, null, null, null);
            if(cur != null){
                if(cur.moveToFirst()){
                    do{
                        User user = new User();
                        user.setUserId(cur.getString(cur.getColumnIndex(USERID)));
                        user.setUsername(cur.getString(cur.getColumnIndex(USERNAME)));
                        user.setUserImage(cur.getString(cur.getColumnIndex(USERIMAGE)));
                        user.setFriend(true);
                        user.setFriendRequest(false);
                        friendsList.add(user);
                    }
                    while(cur.moveToNext());
                }
            }
        }
        finally {
            db.endTransaction();
            assert cur != null;
            cur.close();
        }
        return friendsList;
    }

    public void updateFriend(int id, String username, String userImage) {
        ContentValues cv = new ContentValues();
        cv.put(USERNAME, username);
        cv.put(USERIMAGE, userImage);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteFriend(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteAllFriends() {
        db.delete(TODO_TABLE, null, null);
    }
}