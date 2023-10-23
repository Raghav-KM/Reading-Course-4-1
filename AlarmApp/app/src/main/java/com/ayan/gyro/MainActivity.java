package com.ayan.gyro;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText inclinationAngleEdt;
    private Button checkAnglesBtn;
    private Button stopAlarmBtn;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkAnglesBtn = findViewById(R.id.check_angles_button);
        stopAlarmBtn = findViewById(R.id.stop_alarm_btn);
        inclinationAngleEdt = findViewById(R.id.input_angle_Edt);

        stopAlarmBtn.setVisibility(View.INVISIBLE);
        checkAnglesBtn.setOnClickListener(v -> {
            double input_angle = Double.parseDouble(inclinationAngleEdt.getText().toString());

            // Check if any angle is greater than 40 degrees
            if (input_angle > 40) {
                // Trigger an alarm
                Toast.makeText(getApplicationContext(),"Value out of limit",Toast.LENGTH_SHORT).show();
                playSound();
            }else{
                Toast.makeText(this, "Value in Limit", Toast.LENGTH_SHORT).show();
            }
        });

        stopAlarmBtn.setOnClickListener(view -> {
            if (mp.isPlaying()) {
                mp.setLooping(false);
                mp.stop();
            }
            stopAlarmBtn.setVisibility(View.INVISIBLE);
        });
    }
    private void playSound(){
        stopAlarmBtn.setVisibility(View.VISIBLE);
        mp = MediaPlayer.create(MainActivity.this, R.raw.abc);
        if(!mp.isPlaying()){
            mp.setLooping(true);
            mp.start();
        }
    }



}
