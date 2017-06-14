package com.example.cldme.appthermostat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button homeButton, programButton, settingsButton;
    Intent activityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeButton = (Button) findViewById(R.id.home_button);
        programButton = (Button) findViewById(R.id.program_button);
        settingsButton = (Button) findViewById(R.id.settings_button);

        homeButton.setOnClickListener(this);
        programButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.home_button:
                activityIntent = new Intent(this, MainActivity.class);
                break;
            case R.id.program_button:
                activityIntent = new Intent(this, ProgramActivity.class);
                break;
            case R.id.settings_button:
                activityIntent = new Intent(this, SettingsActivity.class);
                break;
        }
        startActivity(activityIntent);
    }
}
