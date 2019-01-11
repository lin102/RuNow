package com.example.lin.runow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private CameraPosition mCameraPosition;


    private Button btstart = null;
    private Button btstop = null;
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
    private static double sum = 0;
    private static double latitude = 0;
    private static double longitude = 0;
    private static int v=0;
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


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btstart = (Button) findViewById(R.id.button_start);
        btstop = (Button) findViewById(R.id.button_stop);
        texttime = (TextView) findViewById(R.id.data_time);
        textlength = (TextView) findViewById(R.id.data_length);
        textpace = (TextView) findViewById(R.id.data_pace);
        textcalories = (TextView) findViewById(R.id.data_calories);

        btstart.setOnClickListener(listener);
        btstop.setOnClickListener(listener);
        btstop.setEnabled(false);

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
        mLocationRequest.setInterval(1000);
        // the fast interval request is 0.1 second
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
        //the map style here should be changed later with 2 customized styles
        // one for day and one for night
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // enable zoom button
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //enable positioning button
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // add markers
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));



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

    private Location previousLocation = null;
    private ArrayList<Polyline> runningRoute = new ArrayList<Polyline>();
    private ArrayList<Location> points = new ArrayList<Location>();

    public void onLocationChanged(Location location) {
        if (location != null) {

            if(previousLocation == null){
                previousLocation = location;
            }
                 double lat = location.getLatitude();
                 double lon = location.getLongitude();

                 latitude = lat;
                 longitude = lon;
                 latList.add(latitude);
                 lonList.add(longitude);

                // previous coordinates
                System.out.println("previous Location: " + previousLocation.getLatitude() + " " + previousLocation.getLongitude());
                // current coordinates
                System.out.println("Current Location: " + location.getLatitude() + " " + location.getLongitude());

               // set the option of each part of polyline
                PolylineOptions lineOptions = new PolylineOptions()
                        .add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                        .add(new LatLng(location.getLatitude(), location.getLongitude()))
                        // This needs to be beautified
                        .color(R.color.colorAccent)
                        .width(10);

                // add the polyline to the map
                Polyline partOfRunningRoute = mMap.addPolyline(lineOptions);
                // zoom level 17 looks good for running purpose
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(location.getLatitude(),location.getLongitude()), 17));
                // set the zindex so that the poly line stays on top of my tile overlays
                partOfRunningRoute.setZIndex(1000);
                // add the poly line to the array so they can all be removed if necessary
                runningRoute.add(partOfRunningRoute);
                // add the latlng from this point to the array
                points.add(location);
                // store current location as previous location in the end
                previousLocation = location;

        }
    }


    // This is the onclick function of start buttion
    public void onClickStartRuning(View view) {


    }


    /*chronometer*/
    private View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == btstart) {
                startTimer();
                btstart.setEnabled(false);
                btstop.setEnabled(true);
                textlength.setText("0.00");
                textpace.setText("00'00''");
                textcalories.setText("0");
            }
            if (v == btstop) {
                stopTimer();
                btstart.setEnabled(true);
                btstop.setEnabled(false);
            }
        }
    };

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

        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, delay, period);

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
                return "0" + minute + "'0" + second+ "''";
            }
            return "0" + minute + "'" + second+ "''";
        }
        if (second < 10) {
            return minute + ":'0" + second+ "''";
        }
        return minute + "'" + second+ "''";
    }

    /*time format changing*/


    /*test for distance's calculating*/

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
    public int getPace(double length, int t){
        int p = 0;
        p = (int) (t/length);//s/km
        return p;
    }
    /*pace*/

    /*calories*/
    public int getCalories(double length, int t) {
        int c = 0;
//        if (v != 0 && (400/v)!=0) {
////        c = (int)(90*(length)*(30/(400/v))*(t/3600));//default weight = 90
        c = (int) (length + t);//not true
//        }
        return c;
    }
    /*calories*/

    /*time display*/
    public void updateTextView() {
        texttime.setText(getTime(count));
        for (int i = 1; i < latList.size(); i++) {
            s = GetDistance(latList.get(i-1),
                    lonList.get(i-1),
                    latList.get(i),
                    lonList.get(i));
//            Location.distanceBetween(latList.get(i-1),
//                    lonList.get(i-1),
//                    latList.get(i),
//                    lonList.get(i),results);
//            s = results[0];


        }
        sum = sum + s;
        String Sum = String .format("%.2f",sum);
        textlength.setText(Sum);
        v = getPace(sum,count);
        calories = getCalories(sum,count);
        textpace.setText(formatOfPace(v));
        textcalories.setText(calories + "");
    }



}