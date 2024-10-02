
package com.example.dolt.utils;

import static com.example.dolt.DifferentMethods.makeToast;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.dolt.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DatabaseTasks extends SQLiteOpenHelper {


    private static final int VERSION = 1;
    private static final String NAME = "tasksDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TEXT = "text";
    private static final String TASKID = "taskId";
    private static final String USERFROMID = "userFromId";
    private static final String USERTOID = "userToId";
    private static final String TOORFROM = "toOrFrom";
    private static final String DEGREEOFIMPORTANCE = "degreeOfImportance";
    private static final String TERMDATATIME = "termDataTime";
    private static final String ISTASKCHECK = "isTaskCheck";
    private static final String STATUS = "status";
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TEXT + " TEXT, "
            + TASKID + " TEXT, " + USERFROMID + " TEXT, " + USERTOID + " TEXT, " + TOORFROM + " TEXT, "
            + DEGREEOFIMPORTANCE + " INTEGER, " + TERMDATATIME + " TEXT, " + ISTASKCHECK + " INTEGER, " + STATUS + " INTEGER)";

    private SQLiteDatabase db;

    public DatabaseTasks(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        // Create tables again
        onCreate(db);
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    public void insertTask(Task task){
        ContentValues cv = new ContentValues();
        cv.put(TEXT, task.getTaskText());
        cv.put(TASKID, task.getTaskId());
        cv.put(USERFROMID, task.getUserFromId());
        cv.put(USERTOID, task.getUserToId());
        cv.put(TOORFROM, task.getToOrFrom());
        cv.put(DEGREEOFIMPORTANCE, task.getDegreeOfImportance());
        cv.put(TERMDATATIME, task.getTermDateTime());
        cv.put(ISTASKCHECK, task.getIsTaskCheck());
        cv.put(STATUS, task.getTaskStatus());
        db.insert(TODO_TABLE, null, cv);
    }

    public void insertAllTasks(Context context){
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String allTasksStr;
                if (snapshot.child("Tasks").child("tasks").getValue() != null) {
                    allTasksStr = Objects.requireNonNull(snapshot.child("Tasks").child("tasks").getValue()).toString();
                } else {
                    return;
                }
                String[] allTasksIds = allTasksStr.split(",");
                ArrayList<String> allMyTasks = new ArrayList<>();
                if (allTasksIds[0].length()<4) return;
                for (int i = 0; i < allTasksIds.length; i++) {
                    String str = allTasksIds[i];
                    str = str.substring(1, allTasksIds[i].length()-1);
                    if(i==0)
                        str = str.substring(0, allTasksIds[i].length()-2);
                    if (i==allTasksIds.length-1)
                        str = str.substring(0, allTasksIds[i].length()-3);
                    allTasksIds[i] = str;
                    String finalStr = str;
                    if (snapshot.child("Tasks").child(str).child("userFromId").getValue()!=null) {
                        if (Objects.requireNonNull(snapshot.child("Tasks").child(str).child("userFromId").getValue()).toString().equals(uid)
                        || Objects.requireNonNull(snapshot.child("Tasks").child(str).child("userToId").getValue()).toString().equals(uid)) {
                            allMyTasks.add(finalStr);
                        }
                    }
                }

                for (String taskId : allMyTasks){
                    DataSnapshot taskSnapshot = snapshot.child("Tasks").child(taskId);

                    String taskText = Objects.requireNonNull(taskSnapshot.child("taskText").getValue()).toString();
                    String userFromId = Objects.requireNonNull(taskSnapshot.child("userFromId").getValue()).toString();
                    String userToId = Objects.requireNonNull(taskSnapshot.child("userToId").getValue()).toString();
                    String toOrFrom = "";
                    int degreeOfImportance  = ((Long) taskSnapshot.child("degreeOfImportance").getValue()).intValue();
                    String termDateTime = Objects.requireNonNull(taskSnapshot.child("termDateTime").getValue()).toString();
                    int taskStatus = Integer.parseInt(taskSnapshot.child("taskStatus").getValue().toString());
                    int isTaskCheck = Integer.parseInt(taskSnapshot.child("isTaskCheck").getValue().toString());

                    Task task = new Task(0 ,taskText, taskId, userFromId, userToId, toOrFrom, taskStatus, degreeOfImportance, termDateTime, isTaskCheck);
                    openDatabase();
                    insertTask(task);
                    close();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(context, "Не удалось загрузить задачи");
            }
        });
    }

    @SuppressLint("Range")
    public Task getTask(int id) {
        Cursor cur = null;
        Task task = new Task();
        db.beginTransaction();
        try{
            cur = db.query(TODO_TABLE, null, null, null, null, null, null, null);
            if(cur != null){
                if(cur.moveToPosition(id)){
                    task.setId(cur.getInt(cur.getColumnIndex(ID)));
                    task.setTaskText(cur.getString(cur.getColumnIndex(TEXT)));
                    task.setTaskId(cur.getString(cur.getColumnIndex(TASKID)));
                    task.setUserFromId(cur.getString(cur.getColumnIndex(USERFROMID)));
                    task.setUserToId(cur.getString(cur.getColumnIndex(USERTOID)));
                    task.setToOrFrom(cur.getString(cur.getColumnIndex(TOORFROM)));
                    task.setDegreeOfImportance(cur.getInt(cur.getColumnIndex(DEGREEOFIMPORTANCE)));
                    task.setTermDateTime(cur.getString(cur.getColumnIndex(TERMDATATIME)));
                    task.setIsTaskCheck(cur.getInt(cur.getColumnIndex(ISTASKCHECK)));
                    task.setTaskStatus(cur.getInt(cur.getColumnIndex(STATUS)));
                }
            }
        }
        finally {
            db.endTransaction();
            assert cur != null;
            cur.close();
        }
        return task;
    }

    @SuppressLint("Range")public Task getTaskByTaskId(String taskId) {
        Cursor cur = null;
        Task task = new Task();
        db.beginTransaction();
        try {
            cur = db.query(TODO_TABLE, null, TASKID + " = ?", new String[]{taskId}, null, null, null, null);
            if (cur != null && cur.moveToFirst()) {
                task.setId(cur.getInt(cur.getColumnIndex(ID)));
                task.setTaskText(cur.getString(cur.getColumnIndex(TEXT)));
                task.setTaskId(cur.getString(cur.getColumnIndex(TASKID)));
                task.setUserFromId(cur.getString(cur.getColumnIndex(USERFROMID)));
                task.setUserToId(cur.getString(cur.getColumnIndex(USERTOID)));
                task.setToOrFrom(cur.getString(cur.getColumnIndex(TOORFROM)));
                task.setDegreeOfImportance(cur.getInt(cur.getColumnIndex(DEGREEOFIMPORTANCE)));
                task.setTermDateTime(cur.getString(cur.getColumnIndex(TERMDATATIME)));
                task.setIsTaskCheck(cur.getInt(cur.getColumnIndex(ISTASKCHECK)));
                task.setTaskStatus(cur.getInt(cur.getColumnIndex(STATUS)));
            }
        }
        finally {
            db.endTransaction();
            if (cur != null) {
                cur.close();
            }
        }
        return task;
    }

    @SuppressLint("Range")
    public ArrayList<Task> getAllTasks(String katTasks){
        String uid = FirebaseAuth.getInstance().getUid();
        ArrayList<Task> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try{
            cur = db.query(TODO_TABLE, null, null, null, null, null, null, null);
            if(cur != null){
                if(cur.moveToFirst()){
                    do{
                        if (Objects.equals(katTasks, "my")){
                            if(!Objects.equals(cur.getString(cur.getColumnIndex(USERFROMID)), cur.getString(cur.getColumnIndex(USERTOID)))){
                                continue;
                            }
                        } else if (Objects.equals(katTasks, "incoming")){
                            if(Objects.equals(cur.getString(cur.getColumnIndex(USERFROMID)), uid) || !Objects.equals(cur.getString(cur.getColumnIndex(USERTOID)), uid)){
                                continue;
                            }
                        } else if (Objects.equals(katTasks, "sent")){
                            if(!Objects.equals(cur.getString(cur.getColumnIndex(USERFROMID)), uid) || Objects.equals(cur.getString(cur.getColumnIndex(USERTOID)), uid)){
                                continue;
                            }
                        }
                        Task task = new Task();
                        task.setId(cur.getInt(cur.getColumnIndex(ID)));
                        task.setTaskText(cur.getString(cur.getColumnIndex(TEXT)));
                        task.setTaskId(cur.getString(cur.getColumnIndex(TASKID)));
                        task.setUserFromId(cur.getString(cur.getColumnIndex(USERFROMID)));
                        task.setUserToId(cur.getString(cur.getColumnIndex(USERTOID)));
                        task.setToOrFrom(cur.getString(cur.getColumnIndex(TOORFROM)));
                        task.setDegreeOfImportance(cur.getInt(cur.getColumnIndex(DEGREEOFIMPORTANCE)));
                        task.setTermDateTime(cur.getString(cur.getColumnIndex(TERMDATATIME)));
                        task.setIsTaskCheck(cur.getInt(cur.getColumnIndex(ISTASKCHECK)));
                        task.setTaskStatus(cur.getInt(cur.getColumnIndex(STATUS)));
                        taskList.add(task);
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
        return taskList;
    }

    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void updateTask(int id, Task task) {
        ContentValues cv = new ContentValues();
        cv.put(TEXT, task.getTaskText());
        cv.put(TASKID , task.getTaskId());
        cv.put(USERFROMID , task.getUserFromId());
        cv.put(USERTOID , task.getUserToId());
        cv.put(TOORFROM , task.getToOrFrom());
        cv.put(DEGREEOFIMPORTANCE, task.getDegreeOfImportance());
        cv.put(TERMDATATIME, task.getTermDateTime());
        cv.put(ISTASKCHECK, task.getIsTaskCheck());
        cv.put(STATUS, task.getTaskStatus());
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteTask(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteAllTasks() {
        db.delete(TODO_TABLE, null, null);
    }

    public void updateDatabase(Context context){
        deleteAllTasks();
        insertAllTasks(context);
    }

    @Override
    public void close() {
        if (db != null) {
            db.close();
        }
    }
}