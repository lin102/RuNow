package com.example.lin.runow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private CameraPosition mCameraPosition;


    private Button btleft = null;
    private Button btright = null;
    private Button btmiddle = null;
    private TextView texttime = null;
    private TextView textlength = null;
    private TextView textpace = null;
    private TextView textcalories = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private Handler mHandler = null;
    /*out put data*/
    private static int count = 0;
    private static double s = 0;
    private static double d = 0;
    private static double sum = 0;
    private static double latitude = 0;
    private static double longitude = 0;
    private static double vdraw = 0;
    private static int v = 0;
    private static int pace = 0;
    private static int calories = 0;

    private boolean isPause = false;
    private boolean isStop = true;
    private static int delay = 1000; //1s
    private static int period = 1000; //1s
    private static final int UPDATE_TEXTVIEW = 0;

    private static double EARTH_RADIUS = 6378.137;//radius of earth

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    float[] results = new float[3];

    List<Double> latList = new ArrayList<Double>();
    List<Double> lonList = new ArrayList<Double>();

    private Location previousLocation = null;
    private ArrayList<Polyline> runningRoute = new ArrayList<Polyline>();
    private ArrayList<Location> points = new ArrayList<Location>();

    // control the drawing status. default is not drawing
    private boolean isDraw = false;
    //
    private boolean showBound = false;

    String DB_NAME = "running_db.sqlite";
    RunningDAO runningdao;

    private String startTime = null;
    public static final String PREFS_NAME = "prefs";
    public static final String PREF_DARK_THEME = "dark_theme";

    // initialize a set of points of interests
    private LatLng POI_1 = new LatLng(51.0264519, 13.7262368);
    private LatLng POI_2 = new LatLng(51.029106, 13.724735);
    private LatLng POI_3 = new LatLng(51.029029, 13.736457);

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //use the chosen theme
        boolean useDarkTheme = isDarkThemeEnabled();

        if(useDarkTheme) {
            setTheme(R.style.AppThemeDark);
        }
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
         //switch
        Switch toggle = findViewById(R.id.switch1);
        toggle.setChecked(useDarkTheme);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                toggleTheme(isChecked);
            }
        });

        btleft = (Button) findViewById(R.id.button_left);
        btright = (Button) findViewById(R.id.button_right);
        btmiddle = (Button) findViewById(R.id.button_middle);
        texttime = (TextView) findViewById(R.id.data_time);
        textlength = (TextView) findViewById(R.id.data_length);
        textpace = (TextView) findViewById(R.id.data_pace);
        textcalories = (TextView) findViewById(R.id.data_calories);

        btleft.setOnClickListener(listener);
        btmiddle.setOnClickListener(listener);
        btright.setOnClickListener(listener);

        btleft.setEnabled(true);
        btright.setEnabled(false);
        btmiddle.setEnabled(false);
        // connecting running database
        final File dbFile = this.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            try {
                copyDatabaseFile(dbFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // query data from database
        queryDataFromDatabase();// query function


        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TEXTVIEW:
                        updateTextView();
                        break;
                    default:
                        break;
                }
            }
        };


        // google map uses fragment by default not view
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //The function getMapAsync acquires a GoogleMap initializing the map system and the view.
        mapFragment.getMapAsync(this);

        mFusedLocationClient = getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        //set the interval for active location update to 0.3 second
        mLocationRequest.setInterval(300);
        // the fast interval request is 0.01 second
        mLocationRequest.setFastestInterval(100);
        // request High accuracy location based on the need of this app
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        // check for location request permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1340);
        } else {
            requestLocationUpdates();
        }

    }

    private boolean isDarkThemeEnabled() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(PREF_DARK_THEME, false);
    }

    // copy the database file into local from APK
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

    // query data function
    // maybe we can display these old records somewhere later
    public void queryDataFromDatabase() {
        AppDatabase database = Room.databaseBuilder(this, AppDatabase.class, DB_NAME).allowMainThreadQueries().build();
        runningdao = database.getRunningdataDAO();
        List<Runningdata> runningdata_list = runningdao.getAllRuningdata();
        for (int i = 0; i < runningdata_list.size(); i++) {
            int oldId = runningdata_list.get(i).getId();
            String  oldStarttime = runningdata_list.get(i).getStarttime();
            double oldDistance = runningdata_list.get(i).getDistance();
            double oldCalorie = runningdata_list.get(i).getCalorie();
            System.out.println("Database shows here: "+"i:"+i+"oldId:"+oldId +"oldStarttime"+oldStarttime+"oldDistance"+oldDistance+"oldCalorie"+oldCalorie);
        }
        // delete data from database by ID
        //runningdao.deleteById(10);
    }

    // insert records into local database
    public void AddDataRecordtoDB(View view) {

        //get the running distance from textview
        TextView TV_distance = (TextView)findViewById(R.id.data_length);
        Double runningDistance = Double.parseDouble(TV_distance.getText().toString());
        System.out.println("distance is :"+runningDistance);

        //get the calories from textview
        TextView TV_calories = (TextView)findViewById(R.id.data_calories);
        Double runningCalories = Double.parseDouble(TV_calories.getText().toString());
        System.out.println("calorie is :"+runningCalories);

        // new an running "entity"
        Runningdata NewRunningdata = new Runningdata();
        NewRunningdata.setDistance(runningDistance);
        NewRunningdata.setCalorie(runningCalories);
        NewRunningdata.setStarttime(startTime);

        // using "dao" to manipulate database
        runningdao.insert(NewRunningdata);
        System.out.println("I did insertion");
    }


    @SuppressLint("NewApi")
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        // ask for the permission of requesting location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1340);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                isDarkThemeEnabled() ? R.raw.map_night_style : R.raw.map_day_style));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // disenable zoom button because the zoom level is fixed.
        mMap.getUiSettings().setZoomControlsEnabled(false);
        //enable positioning button
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // disable this because after the POI marker popup this tool will be added automatically
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }


    // This function handles the permission result
    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1340:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(this, "Location cannot be obtained due to " + "missing permission.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, null);
    }

    public void onLocationChanged(Location location) {
        if (location != null) {

            if(!showBound) {
                // zoom level 17 looks good in terms of running purpose
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            }
            // drawing the route while the user is running
            if (isDraw) {
                // draw the route
                routeDrawing(location);
            }
            // trigger POI
            triggerPOI();
        }
    }

    // drawing the polyline while running
    private void routeDrawing(Location location) {

        // validity check in case the first point does not have a previous point
        if (previousLocation == null) {
            // copy the current location to the previous point
            previousLocation = location;
        }
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        latitude = lat;
        longitude = lon;
        latList.add(latitude);
        lonList.add(longitude);

        // previous coordinates
        //System.out.println("previous Location: " + previousLocation.getLatitude() + " " + previousLocation.getLongitude());
        // current coordinates
        //System.out.println("Current Location: " + location.getLatitude() + " " + location.getLongitude());

        // Polyline Options differ from
        PolylineOptions lineOptions = new PolylineOptions();
        // set the option of each part of polyline
        // 0.03 is the location update interval also the drawing interval
        vdraw = (GetDistance(previousLocation.getLatitude(), previousLocation.getLongitude(), location.getLatitude(), location.getLongitude()))/0.3;
        System.out.println("vdraw: "+vdraw);
        if (vdraw<0.01){

            lineOptions.add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    // This needs to be beautified
                    .color(getResources().getColor(R.color.slow))
                    .width(30);
            System.out.print("I am running slow");
        }
        if (vdraw>=0.008 && vdraw<=0.03){
            lineOptions.add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    // This needs to be beautified
                    .color(getResources().getColor(R.color.commen))
                    .width(30);
            System.out.print("I am running normally");
        }
        if (vdraw>0.03){
            lineOptions.add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    // This needs to be beautified
                    .color(getResources().getColor(R.color.fast))
                    .width(30);
            System.out.print("I am running fast");
        }

        // add the polyline to the map
        Polyline partOfRunningRoute = mMap.addPolyline(lineOptions);
        // set the zindex so that the poly line stays on top of my tile overlays
        partOfRunningRoute.setZIndex(1000);
        // add the poly line to the array so they can all be removed if necessary
        runningRoute.add(partOfRunningRoute);
        // add the latlng from this point to the array
        points.add(location);
        // store current location as previous location in the end
        previousLocation = location;
    }

    // determine if user arrive the point of interest
    private void triggerPOI() {
        double distanceThreshold = 0.01;// unit is km
        // the distance to the target // the unit is km
        double distanceToPOI_1 = GetDistance(POI_1.latitude, POI_1.longitude, latitude, longitude);
        double distanceToPOI_2 = GetDistance(POI_2.latitude, POI_2.longitude, latitude, longitude);
        double distanceToPOI_3 = GetDistance(POI_3.latitude, POI_3.longitude, latitude, longitude);
        //System.out.println("The distance to POI is " + distanceToPOI);
        // 200km per degree
        if (distanceToPOI_1 < distanceThreshold) {
            mMap.addMarker(
                    new MarkerOptions()
                            //   .icon(BitmapDescriptorFactory.fromResource(R.drawable.star))
                            .position(POI_1)
                            .anchor(0.5f, 0.5f)
                            .title("Alte Mensa")
                            .snippet("---1st Target---"));
        }
        if (distanceToPOI_2 < distanceThreshold) {
            mMap.addMarker(
                    new MarkerOptions()
                            //   .icon(BitmapDescriptorFactory.fromResource(R.drawable.star))
                            .position(POI_2)
                            .anchor(0.5f, 0.5f)
                            .title("Helmholtzstraße ")
                            .snippet("---2nd Target---"));
        }
        if (distanceToPOI_3 < distanceThreshold) {
            mMap.addMarker(
                    new MarkerOptions()
                            //   .icon(BitmapDescriptorFactory.fromResource(R.drawable.star))
                            .position(POI_3)
                            .anchor(0.5f, 0.5f)
                            .title("SLUB")
                            .snippet("---3rd Target---"));
        }
    }

    /*chronometer*/
    private View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == btleft) {//start running

                previousLocation = null;
                // start drawing
                isDraw = true;
                // camera is now showing bounds of points
                showBound = false;

                // clear the running route  array list if it is not empty
                if(!runningRoute.isEmpty()) {
                    // remove all the polylines from the map
                    for (Polyline line : runningRoute) {
                        line.remove();
                    }
                    runningRoute.clear();
                }

                isStop = !isStop;
                btleft.setEnabled(false);
                btleft.setBackgroundColor(getResources().getColor(R.color.unabled));
                btright.setEnabled(true);
                btright.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btmiddle.setEnabled(true);
                startTimer();
                btmiddle.setBackgroundResource(R.drawable.pause_button);
                textlength.setText("0.00");
                textpace.setText("00'00''");
                textcalories.setText("0");

                //get the current time from device system and parse to String
                startTime = Calendar.getInstance().getTime().toString();
                System.out.println("Current time is: " + startTime);
                //Tue Jan 15 13:12:49 GMT 2019
            }

            if (v == btmiddle) {
                btleft.setEnabled(false);
                btleft.setBackgroundColor(getResources().getColor(R.color.unabled));
                btright.setEnabled(true);
                btmiddle.setEnabled(true);
                pauseTimer();

                if (isPause) {//pause
                    btmiddle.setBackgroundResource(R.drawable.play_button);
                    //stop drawing
                    isDraw = false;
                }else{//resume
                    btmiddle.setBackgroundResource(R.drawable.pause_button);
                    previousLocation = null;
                    isDraw = true;
                }
            }

            if (v == btright){//stop
                stopButton();


                AddDataRecordtoDB(v);
            }
        }
    };

    private void stopButton() {
        if (isPause){
            isPause = !isPause;
        }
        stopTimer();
        sum = 0;
        btmiddle.setBackgroundResource(R.drawable.logo_round);
        btleft.setEnabled(true);
        btleft.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btmiddle.setEnabled(false);
        btright.setEnabled(false);
        btright.setBackgroundColor(getResources().getColor(R.color.unabled));
        isDraw = false;
        // add record to database

        showBound = true;
        showBounds();
        // clear points arraylist
        points.clear();
    }

    // get the bound of all the points
    private void showBounds(){

        LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();


        for (int i = 0; i < points.size(); i++) {
            LatLng b_position = new LatLng(points.get(i).getLatitude(),points.get(i).getLongitude());
            boundBuilder.include(b_position);
        }

        LatLngBounds bounds = boundBuilder.build();

        int deviceWidth = getResources().getDisplayMetrics().widthPixels;
        int deviceHeight = getResources().getDisplayMetrics().heightPixels;
        int devicePadding = (int) (deviceHeight * 0.20); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, deviceWidth, deviceHeight, devicePadding);
        mMap.animateCamera(cu);

    }


    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {

                    sendMessage(UPDATE_TEXTVIEW);
                    do {
                        try {

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);
                    count++;/*update time*/
//                    sum = sumDistance(latitude,longitude);/*update distance*/
                }
            };
        }

        if (mTimer != null)
            mTimer.schedule(mTimerTask, delay, period);

    }

    private void pauseTimer(){
        isPause = !isPause;
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;

        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;

        }
        count = 0;
    }

    public void sendMessage(int id) {
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }

    /*chronometer*/
    /*time format changing*/
    public static String getTime(int second) {
        if (second < 10) {
            return "00:00:0" + second;
        }
        if (second < 60) {
            return "00:00:" + second;
        }
        if (second < 3600) {
            int minute = second / 60;
            second = second - minute * 60;
            if (minute < 10) {
                if (second < 10) {
                    return "00:" + "0" + minute + ":0" + second;
                }
                return "00:" + "0" + minute + ":" + second;
            }
            if (second < 10) {
                return "00:" + minute + ":0" + second;
            }
            return "00:" + minute + ":" + second;
        }
        int hour = second / 3600;
        int minute = (second - hour * 3600) / 60;
        second = second - hour * 3600 - minute * 60;
        if (hour < 10) {
            if (minute < 10) {
                if (second < 10) {
                    return "0" + hour + ":0" + minute + ":0" + second;
                }
                return "0" + hour + ":0" + minute + ":" + second;
            }
            if (second < 10) {
                return "0" + hour + ":" + minute + ":0" + second;
            }
            return "0" + hour + ":" + minute + ":" + second;
        }
        if (minute < 10) {
            if (second < 10) {
                return hour + ":0" + minute + ":0" + second;
            }
            return hour + ":0" + minute + ":" + second;
        }
        if (second < 10) {
            return hour + ":" + minute + ":0" + second;
        }
        return hour + ":" + minute + ":" + second;
    }

    public static String formatOfPace(int second) {
        if (second < 10) {
            return "00'0" + second + "''";
        }
        if (second < 60) {
            return "00'" + second + "''";
        }
        int minute = second / 60;
        second = second - minute * 60;
        if (minute < 10) {
            if (second < 10) {
                return "0" + minute + "'0" + second + "''";
            }
            return "0" + minute + "'" + second + "''";
        }
        if (second < 10) {
            return minute + ":'0" + second + "''";
        }
        return minute + "'" + second + "''";
    }

    /*time format changing*/

    /*distance's calculating*/
    // We are not using google built in method to get the distacne between 2 points
    // This is a method we learned from Land surveying class, it works pretty good and
    // it is a very good chance for us to implement it in our project
    public static double GetDistance(double lat1, double lon1, double lat2, double lon2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;//km
//        Log.e("s", "s=" + s);
        return s;
    }

    /*pace*/
    /*pace*/
    public int getPace(double length, int t) {
        int p = 0;
        p = (int) (t / length);//s/km
        return p;
    }
    /*pace*/

    /*calories*/
    public int getCalories(double dis, int t) {
        int c = 0;
        c = (int)((dis + t)*0.25);
        return c;
    }
    /*calories*/

    /*time display*/
    public void updateTextView() {
        texttime.setText(getTime(count));
        for (int i = 1; i < latList.size(); i++) {
            s = GetDistance(latList.get(i - 1),
                    lonList.get(i - 1),
                    latList.get(i),
                    lonList.get(i));

//            Location.distanceBetween(latList.get(i-1),
//                    lonList.get(i-1),
//                    latList.get(i),
//                    lonList.get(i),results);
//            s = results[0];
        }
        for (int j=10; j < latList.size(); j=j+10 ){
            d = GetDistance(latList.get(j - 10),
                    lonList.get(j - 10),
                    latList.get(j),
                    lonList.get(j));
            pace = getPace(d, 10);
            textpace.setText(formatOfPace(pace));
        }
        sum = sum + s;
        String Sum = String .format("%.2f",sum);
        textlength.setText(Sum);

        v = getPace(sum,count);
        calories = getCalories(sum, count);
        textcalories.setText(calories + "");
    }

    private void toggleTheme(boolean darkTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_DARK_THEME, darkTheme);
        editor.apply();

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                darkTheme ? R.raw.map_night_style : R.raw.map_day_style));

        // since we need to recreate the activity when switching a theme, at least make sure to stop
        // the timer and store data, .... if we are running
        if (btright.isEnabled()) {
            stopButton();
        }
        recreate();
    }
    public void onClickStartNewActivity(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}