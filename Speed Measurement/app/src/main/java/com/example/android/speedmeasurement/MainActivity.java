package com.example.android.speedmeasurement;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private LocationManager locationManager;
    private TextView speedTextView;
    private Button button;
    private boolean isSpeedometerRunning = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedTextView = findViewById(R.id.speedTextView);
        button = findViewById(R.id.button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        button.setOnClickListener(view -> {
            if (!isSpeedometerRunning) {
                startSpeedometer();
            } else {
                stopSpeedometer();
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location==null){
            speedTextView.setText("Speed: -.- km/h");
        } else {
            float nCurrentSpeed = location.getSpeed() * 3.6f;
            speedTextView.setText(String.format("%.2f", nCurrentSpeed)+ " km/h" );
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void startSpeedometer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            isSpeedometerRunning = true;
            button.setText("Stop");
        }
    }

    private void stopSpeedometer() {
        locationManager.removeUpdates(this);
        isSpeedometerRunning = false;
        button.setText("Start");
        speedTextView.setText("Speed: -.- km/h");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeedometer();
            }
        }
    }
}
