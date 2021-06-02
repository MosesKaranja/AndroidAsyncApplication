package com.example.syncingapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    //DB class for DB related operations
    DBController controller = new DBController(this);

    //Progress Dialog Object
    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get User records from SQLite DB
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();

        if (userList.size() != 0){
            //Set the User Array list in Listview
            ListAdapter adapter = new SimpleAdapter( MainActivity.this,userList, R.layout.activity_view_user_entry, new String[] { "userId","userName"}, new int[] {R.id.userId, R.id.userName});
            ListView myList = findViewById(R.id.list);
            myList.setAdapter(adapter);

            Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();

        }
        //Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Synching SQLite Data with Remote MySQL DB. Please wait...");
        prgDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu, this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh){
            syncSQLiteMySQLDB();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void addUser(View view){
        Intent objIntent = new Intent(getApplicationContext(), NewUser.class);
        startActivity(objIntent);

    }

    public void syncSQLiteMySQLDB(){
        //Create AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.setConnectTimeout(30000);
        client.setResponseTimeout(30000);

        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();

        Log.i("userList", String.valueOf(userList));

        if (userList.size() !=0 ){
            if (controller.dbSyncCount() != 0){
                prgDialog.show();
                params.put("usersJSON", controller.composeJSONFromSQLite());
                client.post("http://172.16.40.105/AndroidSyncBackend/insertuser.php", params, new AsyncHttpResponseHandler() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i("responseBody", String.valueOf(responseBody));
                        prgDialog.hide();
                        try {
                            JSONArray arr = new JSONArray(responseBody);
                            for (int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject) arr.get(i);
                                Log.i("objId", (String) obj.get("id"));
                                Log.i("status", (String) obj.get("status"));

                                controller.updateSyncStatus(obj.get("id").toString(), obj.get("status").toString());

                            }

                            Toast.makeText(getApplicationContext(), "DB SYNC COMPLETED!", Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        prgDialog.hide();
                        if (statusCode == 404){
                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();

                        }
                        else if(statusCode == 500){
                            Toast.makeText(getApplicationContext(), "Something went wrong at sever end.", Toast.LENGTH_LONG).show();

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            } else{
                Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();

            }

        } else{
            Toast.makeText(getApplicationContext(), "No data in SQLite DB, please do enter User name to perform Sync action", Toast.LENGTH_LONG).show();
        }

        }

    }