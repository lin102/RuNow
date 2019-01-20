package com.example.lin.runow;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class HistoryActivity extends AppCompatActivity {

    String DB_NAME = "running_db.sqlite";
    RunningDAO runningdao;
    LinearLayout data_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        data_layout = findViewById(R.id.data_layout);

        final File dbFile = this.getDatabasePath(DB_NAME);
        if(!dbFile.exists()){
            try {
                copyDatabaseFile(dbFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        queryDataFromDatabase();
    }
    private void copyDatabaseFile(String destinationPath) throws IOException {
        InputStream assetsDB = this.getAssets().open(DB_NAME);
        OutputStream dbOut = new FileOutputStream(destinationPath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = assetsDB.read(buffer)) > 0) {
            dbOut.write(buffer, 0, length);
        }
        dbOut.flush();
        dbOut.close();
    }

    public void queryDataFromDatabase() {
        AppDatabase database = Room.databaseBuilder(this, AppDatabase.class, DB_NAME).allowMainThreadQueries().build();
        runningdao = database.getRunningdataDAO();
        List<Runningdata> runningdata_list = runningdao.getAllRuningdata();
        for (int i = 0; i < runningdata_list.size(); i++) {
            int oldId = runningdata_list.get(i).getId();
            String  oldStarttime = runningdata_list.get(i).getStarttime();
            double oldDistance = runningdata_list.get(i).getDistance();
            double oldCalorie = runningdata_list.get(i).getCalorie();
//            System.out.println("Database shows here: "+"i:"+i+"oldId:"+oldId +"oldStarttime"+oldStarttime+"oldDistance"+oldDistance+"oldCalorie"+oldCalorie);

            View ViewToAdd = LayoutInflater.from(this)
                    .inflate(R.layout.item_data, null);

            TextView data_id = ViewToAdd.findViewById(R.id.data_id);
            TextView data_distance = ViewToAdd.findViewById(R.id.data_distance);
            TextView data_starttime = ViewToAdd.findViewById(R.id.data_starttime);
            TextView data_calories = ViewToAdd.findViewById(R.id.data_calories);

            data_id.setText(oldId + "   ");
            data_distance.setText(" " + oldDistance + "");
            data_calories.setText(oldCalorie + " ");
            data_starttime.setText(oldStarttime);


            data_layout.addView(ViewToAdd);

        }
        // delete data from database by ID
        //runningdao.deleteById(10);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}