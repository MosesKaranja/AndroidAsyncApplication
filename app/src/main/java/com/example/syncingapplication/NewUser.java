package com.example.syncingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class NewUser extends AppCompatActivity {
    EditText userName;
    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        userName = (EditText) findViewById(R.id.userName);

    }

    public void addNewUser(View view) {
        HashMap<String, String> queryValues = new HashMap<String, String>();
        queryValues.put("userName", userName.getText().toString());
        Log.i("hashMap", String.valueOf(queryValues));

        if (userName.getText().toString() != null
                && userName.getText().toString().trim().length() != 0) {
            controller.insertUser(queryValues);
            this.callHomeActivity(view);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter User name", Toast.LENGTH_LONG).show();
        }
    }

    public void callHomeActivity(View view) {
        Intent objIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(objIntent);
    }

    public void cancelAddUser(View view) {
        this.callHomeActivity(view);
    }

}