package com.example.syncingapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class DBController extends SQLiteOpenHelper {
//    public DBController(@Nullable Context context) {
//        super(context, name, factory, version);
//    }

    public DBController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query;
        query="CREATE TABLE users ( userId INTEGER PRIMARY KEY, userName TEXT, updateStatus TEXT)";
        sqLiteDatabase.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String query;
        query = "DROP TABLE users";
        sqLiteDatabase.execSQL(query);
        onCreate(sqLiteDatabase);

    }

    //Insert user into SQLite DB
    public void insertUser(HashMap<String, String> queryValues){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName", queryValues.get("userName"));
        values.put("updateStatus", "no");
        database.insert("users",null,values);
        database.close();

    }

    //Get list of users from SQLite DB as Array List
    public ArrayList<HashMap<String, String>> getAllUsers(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM users";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("userId",cursor.getString(0));
                map.put("userName",cursor.getString(1));
                wordList.add(map);
            }
            while (cursor.moveToNext());

        }
        database.close();
        return wordList;

    }

    //Compose JSON out of SQLite records
    public String composeJSONFromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT  * FROM users where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        Log.i("cursor0", cursor.getColumnName(0));
        Log.i("cursor1", cursor.getColumnName(1));

        Log.i("cursor", String.valueOf(cursor.getCount()));

        //Log.i("cursorGetString0", cursor.getString(0));
        //Log.i("cursorGetString1", cursor.getString(1));
        //cursor.get

//        Log.i("cursorGetPositionOut", String.valueOf(cursor.getPosition()));
//
//        while (cursor.moveToNext()){
//            Log.i("cursorLoop",cursor.getString(0));
//            Log.i("cursorLoopName",cursor.getString(1));
//            Log.i("cursorGetPositionIn", String.valueOf(cursor.getPosition()));
//
//        }


        if (cursor.moveToFirst()){
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("userId", cursor.getString(0));
                map.put("userName", cursor.getString(1));
                //map.put("userId", cursor.getString("userId"));
                //map.put("userName", cursor.getString("userName"));
                wordList.add(map);
            }
            while (cursor.moveToNext());
        }

//        while (cursor.moveToNext()){
//                HashMap<String, String> map = new HashMap<String, String>();
//                map.put("userId", cursor.getString(0));
//                map.put("userName", cursor.getString(1));
//                Log.i("HashMap", String.valueOf(map));
//                wordList.add(map);
//
//
//
//        }

        Log.i("wordList", String.valueOf(wordList));

        database.close();
        Gson gson = new GsonBuilder().create();

        return gson.toJson(wordList);

    }

    //Get Sync Status of SQLite
    public String getSyncStatus(){
        String msg = null;
        if (this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
            //return true;

        }
        else{
            msg = "DB Sync needed";

        }
        return msg;
    }

    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM users where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    //Update Sync Status against each User ID

    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update users set updateStatus = '"+ status +"' where userId="+"'"+ id +"'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


}
