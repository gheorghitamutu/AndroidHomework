package com.example.androidhomework;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SensorActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int MIN_TIME_BW_UPDATES = 10000;
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 10000;
    private static final String TAG = "SensorsActivity";
    private static final String MY_SENSOR_NAME = "GPS COORDINATES";

    private LocationManager locationManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final SensorManager sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        final List<Sensor> sensorList =
                Objects.requireNonNull(sensorManager).getSensorList(Sensor.TYPE_ALL);

        ArrayList<String> sensors = new ArrayList<>(Collections.singleton(MY_SENSOR_NAME));
        sensorList.forEach(s -> sensors.add(s.getName()));

        final ListView list = findViewById(R.id.list_activity);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensors);
        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {

            sensorManager.unregisterListener(SensorActivity.this);

            String clickedItemText = (String) list.getItemAtPosition(position);

            for (Sensor s : sensorList) {
                if (s.getName().equals(clickedItemText)) {
                    sensorManager.registerListener(SensorActivity.this, s, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    sensorManager.unregisterListener(SensorActivity.this, s);
                }
            }

            if (clickedItemText.equals(MY_SENSOR_NAME)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SensorActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                        requestPermissionsGps();
                    } else {
                        Location location = getLocation();

                        String locationData = "GPS Coordinates\n" +
                                "Longitude " +
                                location.getLongitude() +
                                "\n" +
                                "Latitude " +
                                location.getLatitude() +
                                "\n" +
                                "Altitude " +
                                location.getAltitude() +
                                "\n";

                        final TextView sensorTextView = findViewById(R.id.sensorTextView);
                        sensorTextView.setText(locationData);
                    }
                }
            }

        });
    }

    public Location getLocation() {
        Location location = null;

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= 23) { // Marshmallow
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    requestPermissionsGps();
                }
            }

            final String[] providers = {
                    LocationManager.GPS_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER,
                    LocationManager.NETWORK_PROVIDER
            };

            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(LOCATION_SERVICE);

            for (String provider : providers) {
                if (Objects.requireNonNull(locationManager).isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                            provider,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d(TAG, String.format("%s Enabled", provider));

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void requestPermissionsGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Permission required for location coordinates.")
                        .setPositiveButton("ok", (dialog, which) -> requestPermissions(
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                },
                                LOCATION_PERMISSION_REQUEST_CODE))
                        .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            String access;
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                access = "granted";
            } else {
                access = "denied";
            }

            Toast.makeText(SensorActivity.this, String.format("Permission %s", access), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String content =
                String.format("%s %s", event.sensor.getName(), Arrays.toString(event.values));

        final TextView sensorTextView = findViewById(R.id.sensorTextView);
        sensorTextView.setText(content);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    @Override
    public void onLocationChanged(Location location) {
        //
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //
    }

    @Override
    public void onProviderEnabled(String provider) {
        //
    }

    @Override
    public void onProviderDisabled(String provider) {
        //
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
