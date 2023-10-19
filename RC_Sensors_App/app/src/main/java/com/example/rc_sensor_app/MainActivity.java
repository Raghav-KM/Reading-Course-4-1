package com.example.rc_sensor_app;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /* ----------------- UI ELEMENTS ----------------- */
    private Button startMonitoringLightBtn;
    private Button endMonitoringLightBtn;
    private TextView lightSensorReadingTxt;
    private Button startMonitoringOrientationBtn;
    private Button endMonitoringOrientationBtn;
    private TextView xReadingTxt;
    private TextView yReadingTxt;
    private TextView zReadingTxt;

    /* -------- Sensor Related Member Variables -------- */

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor lightSensor;

    boolean isMonitoringLight = false;
    boolean isMonitoringOrientation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initSensors();

        startMonitoringLightBtn.setOnClickListener(view -> {
            isMonitoringLight = true;
        });
        endMonitoringLightBtn.setOnClickListener(view -> {
            isMonitoringLight = false;
            lightSensorReadingTxt.setText(Float.toString(0.00F));
        });

        startMonitoringOrientationBtn.setOnClickListener(view -> {
            isMonitoringOrientation = true;
        });
        endMonitoringOrientationBtn.setOnClickListener(view -> {
            isMonitoringOrientation = false;
            xReadingTxt.setText("X = " + 0.0F);
            yReadingTxt.setText("Y = " + 0.0F);
            zReadingTxt.setText("Z = " + 0.0F);
        });


    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        } else {
            Toast.makeText(this, "One or More Sensors not available!", Toast.LENGTH_SHORT).show();
            startMonitoringLightBtn.setVisibility(View.INVISIBLE);
            endMonitoringLightBtn.setVisibility(View.INVISIBLE);
            startMonitoringOrientationBtn.setVisibility(View.INVISIBLE);
            startMonitoringOrientationBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void initUI() {
        startMonitoringLightBtn = findViewById(R.id.startMonitoringLight_btn);
        endMonitoringLightBtn = findViewById(R.id.endMonitoringLight_btn);
        lightSensorReadingTxt = findViewById(R.id.lightSensorReading_tv);

        startMonitoringOrientationBtn = findViewById(R.id.startMonitoringOrientation_btn);
        endMonitoringOrientationBtn = findViewById(R.id.endMonitoringOrientation_btn);
        xReadingTxt = findViewById(R.id.xReading_txt);
        yReadingTxt = findViewById(R.id.yReading_txt);
        zReadingTxt = findViewById(R.id.zReading_txt);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {

            float sensorValue = (float) sensorEvent.values[0];
            if(isMonitoringLight)
                lightSensorReadingTxt.setText(Float.toString(sensorValue));
            else
                lightSensorReadingTxt.setText(Float.toString(0.00F));

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = Math.round(sensorEvent.values[0]);
            float y = Math.round(sensorEvent.values[1]);
            float z = Math.round(sensorEvent.values[2]);

            if(isMonitoringOrientation){
                xReadingTxt.setText("X = " + x);
                yReadingTxt.setText("Y = " + y);
                zReadingTxt.setText("Z = " + z);
            }else{
                xReadingTxt.setText("X = " + 0.0F);
                yReadingTxt.setText("Y = " + 0.0F);
                zReadingTxt.setText("Z = " + 0.0F);
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerSensor != null) {
            sensorManager.unregisterListener(this);
        }
        if (lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }
}