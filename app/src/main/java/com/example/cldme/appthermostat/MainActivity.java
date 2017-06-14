package com.example.cldme.appthermostat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.thermostatapp.util.HeatingSystem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button homeButton, programButton, settingsButton;
    private ImageButton plusButton, minusButton;
    private TextView serverDay, serverTime;
    private TextView currentTemp, targetTemp;
    private TextView dayTemp, nightTemp;
    private SeekBar seekBar;

    private Double currentTempVal, targetTempVal;

    private static Double tempMin = 5.0;
    private static Double tempMax = 30.0;

    Intent activityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeButton = (Button) findViewById(R.id.home_button);
        programButton = (Button) findViewById(R.id.program_button);
        settingsButton = (Button) findViewById(R.id.settings_button);
        plusButton = (ImageButton) findViewById(R.id.plus_button);
        minusButton = (ImageButton) findViewById(R.id.minus_button);
        serverDay = (TextView) findViewById(R.id.server_day);
        serverTime = (TextView) findViewById(R.id.server_time);
        currentTemp = (TextView) findViewById(R.id.current_temperature);
        targetTemp = (TextView) findViewById(R.id.target_temperature);
        dayTemp = (TextView) findViewById(R.id.day_temperature);
        nightTemp = (TextView) findViewById(R.id.night_temperature);
        seekBar = (SeekBar) findViewById(R.id.temperature_seek_bar);

        homeButton.setOnClickListener(this);
        programButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        plusButton.setOnClickListener(this);
        minusButton.setOnClickListener(this);

        //Configuring variables for communicating with the server
        HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/58";
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";

        Thread setup = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Get all the information from the server
                    final Double currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                    final Double targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));

                    //Update the main variable for the current and target temperatures
                    currentTempVal = currentTemperature;
                    targetTempVal = targetTemperature;

                } catch (Exception e) {
                    System.err.println("Error occured " + e);
                }
            }
        });

        //Start the setup thread and then join it with the main thread
        //This is done so we can wait for the thread to initialize the temperature values (got from the server)
        setup.start();

        try {

            //Wait for the setup thread to finish then continue with the program
            setup.join();

        } catch (Exception e) {
            System.err.print("Error occured " + e);
        }

        //-------------------- FROM HERE WE CAN USE targetTemp and currentTemp VALUES --------------------\\

        //SeekBar maximum can be 250 (specified in the app requirements)
        seekBar.setMax(250);
        //Set the seekBar listener
        seekBar.setOnSeekBarChangeListener(seekBarListener);
        //Set the progress of the seekBar
        updateSeekBar(targetTempVal);

        new Thread() {

            @Override
            public void run() {
                try {
                    //In here we can update the UI elements while retrieving new data from the server
                    while(!isInterrupted()) {
                        //Get all the information from the server
                        final String currentDay = HeatingSystem.get("day");
                        final String currentTime = HeatingSystem.get("time");
                        final String dayTempString = HeatingSystem.get("dayTemperature");
                        final String nightTempString = HeatingSystem.get("nightTemperature");
                        final Double currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                        final Double targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));

                        //When we update UI elements we use runOnUiThread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Set the text for diferent UI elements on the home screen
                                serverDay.setText(currentDay);
                                serverTime.setText(currentTime);

                                currentTemp.setText(String.valueOf(currentTemperature) + " \u2103");
                                //targetTemp.setText(String.valueOf(targetTemperature) + " \u2103");

                                dayTemp.setText(dayTempString + " \u2103");
                                nightTemp.setText(nightTempString + " \u2103");
                            }
                        });
                        //Wait some time until new information is generated on the server
                        Thread.sleep(100);
                    }
                } catch(Exception e) {
                    System.err.println("Error occured " + e);
                }
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.home_button:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.program_button:
                startActivity(new Intent(this, ProgramActivity.class));
                break;
            case R.id.settings_button:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.plus_button:
                addTemperature();
                break;
            case R.id.minus_button:
                decreaseTemperature();
                break;
        }
    }

    //Call this function each time the plus/minus buttons are pressed
    public void updateSeekBar(double temp) {
        seekBar.setProgress((int)((temp / 0.1) - tempMin * 10));
    }

    public SeekBar.OnSeekBarChangeListener seekBarListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Calculate the temperature from the seekBar then update the temperature text
                    double temp = (double)(tempMin + Math.round(progress) / 10.0);
                    //Update the targetTemp variable (!important)
                    targetTempVal = temp;
                    setTemp(temp);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    private void addTemperature() {
        //Check if temp is above 5 and below 30, only then we update the temperature
        if(targetTempVal >= 5 && targetTempVal < 30) {
            targetTempVal = Math.round((targetTempVal + 0.1) * 10.0) / 10.0 ;
            setTemp(targetTempVal);
            //Update the seekBar progress with the new temp
            updateSeekBar(targetTempVal);
        }
    }

    private void decreaseTemperature() {
        //Check if temp is above 5 and below 30, only then we update the temperature
        if(targetTempVal > 5 && targetTempVal <= 30) {
            targetTempVal = targetTempVal - 0.09;
            setTemp(targetTempVal);
            //Update the seekBar progress with the new temp
            updateSeekBar(targetTempVal);
        }
    }

    private void setTemp(final double temp) {

        targetTemp.setText(String.valueOf(temp) + " \u2103");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put("targetTemperature", String.valueOf(temp));
                } catch(Exception e) {
                    System.err.println("Error occured " + e);
                }
            }
        }).start();
    }
}
