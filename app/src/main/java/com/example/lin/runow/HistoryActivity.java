package com.example.lin.runow;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    ScrollView data_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(MapsActivity.PREFS_NAME, MODE_PRIVATE);
        if(preferences.getBoolean(MapsActivity.PREF_DARK_THEME,false)){
            setTheme(R.style.AppThemeDarkDialog);
        }

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

            View ViewToAdd = LayoutInflater.from(this)
                    .inflate(R.layout.item_data, null);

            //Button data_deleteItem = (Button) ViewToAdd.findViewById(R.id.button_deleteItem);

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

            case R.id.action_clear:
                data_list = findViewById(R.id.data_list);
                data_list.removeAllViewsInLayout();
                runningdao.deleteall();
        }
        return super.onOptionsItemSelected(item);
    }

     // detele item by click
    public void onClickDeleteItem(View view){

        // get the LinearLayout of this item
        LinearLayout r = (LinearLayout) ((ViewGroup) view.getParent());
        TextView deleteView = (TextView) r.getChildAt(1);
        //int deleteId = Integer.parseInt(deleteView.getText().toString());
        Double deleteId = Double.parseDouble(deleteView.getText().toString());
        Integer deleteIntId = deleteId.intValue();
        System.out.println("The delete id is : "+deleteIntId);
        // delete data from database by ID
        runningdao.deleteById(deleteIntId);
        //remove this linearlayout row
        ((ViewGroup) r.getParent()).removeView(r);


    }


}
