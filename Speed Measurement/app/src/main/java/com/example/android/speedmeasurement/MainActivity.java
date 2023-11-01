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
    private static final int UPDATE_INTERVAL = 1000; // Update speed every second
    private static final float ALPHA = 0.2f; // Low-pass filter coefficient

    private Location lastLocation;

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
        if (location == null) {
            speedTextView.setText("Speed: -.- km/h");
        } else {
            float nCurrentSpeed = location.getSpeed() * 3.6f; // Convert speed to km/h

            // Use a low-pass filter to smooth speed readings
            if (lastLocation != null) {
                nCurrentSpeed = (ALPHA * nCurrentSpeed) + ((1 - ALPHA) * lastLocation.getSpeed() * 3.6f);
            }

            if (nCurrentSpeed >= 0) {
                speedTextView.setText(String.format("Speed: %.2f km/h", nCurrentSpeed));
            } else {
                speedTextView.setText("Speed: -.- km/h");
            }

            lastLocation = location;
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
            // Use the best available location provider for accuracy
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0, this);
            isSpeedometerRunning = true;
            button.setText("Stop");
        }
    }

    private void stopSpeedometer() {
        locationManager.removeUpdates(this);
        isSpeedometerRunning = false;
        button.setText("Start");
        speedTextView.setText("Speed: -.- km/h");
        lastLocation = null;
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
