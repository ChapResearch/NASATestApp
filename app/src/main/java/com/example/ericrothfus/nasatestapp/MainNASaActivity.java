package com.example.ericrothfus.nasatestapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class MainNASaActivity extends AppCompatActivity {

    Switch onSwitch;

    int match = 100;		// just a place to start

    Button nextMatchButton;
    Button transmitButton;
    Button startButton;
    Button stopButton;
    Button resetButton;

    NASA_BLE ble;

    private Handler connectionReportHandler;

    private final NASA_BLE_Interface bleCallbacks = new NASA_BLE_Interface() {

	    @Override
	    public String NASA_controllerName() {

		// TODO - these should access some UI component for the name of the controller
		//        or even go find the bluetooth name
		
		return (new String("floopy"));
	    }
    
	    @Override
	    public String NASA_password() {

		// TODO - these should access some UI component for the password
		
		return (new String("doopy"));
	    }

	    @Override
	    public String NASA_match() {

		// TODO - these should access some UI component for the match number
		
		return (Integer.toString(match));
	    }

	    @Override
	    public String NASA_competition() {

		// TODO - these should access some UI component for the name of the competition
		
		return (new String("El Paso"));
	    }
    
	    @Override
	    public String NASA_year() {

		// TODO - these should access some UI component for the year of the competition
		
		return (new String("2021"));
	    }
    
	    @Override
	    public void NASA_slotChange(int slot, boolean claimed) {
		RadioButton indicator = null;

		switch (slot) {
		case 0:	indicator = (RadioButton) findViewById(R.id.a_indicator); break;
		case 1:	indicator = (RadioButton) findViewById(R.id.b_indicator); break;
		case 2:	indicator = (RadioButton) findViewById(R.id.c_indicator); break;
		case 3:	indicator = (RadioButton) findViewById(R.id.d_indicator); break;
		case 4:	indicator = (RadioButton) findViewById(R.id.e_indicator); break;
		case 5:	indicator = (RadioButton) findViewById(R.id.f_indicator); break;
		}
		indicator.setChecked(claimed);
	    }

	    @Override
	    public void NASA_teamColor(int slot, int givenColor)	// 0x00 gray, 0x01 blue, 0x02 red, else gray
	    {
		EditText teamColor;
		switch(slot) {
		default:
		case 0:    	teamColor = (EditText) findViewById(R.id.a_color); break;
		case 1:    	teamColor = (EditText) findViewById(R.id.b_color); break;
		case 2:    	teamColor = (EditText) findViewById(R.id.c_color); break;
		case 3:    	teamColor = (EditText) findViewById(R.id.d_color); break;
		case 4:    	teamColor = (EditText) findViewById(R.id.e_color); break;
		case 5:    	teamColor = (EditText) findViewById(R.id.f_color); break;
		}
			    
		int color;
		switch(givenColor) {
		case 1:		color = Color.BLUE; break;
		case 2:		color = Color.RED; break;
		default:	color = Color.LTGRAY; break;
		}

		teamColor.setBackgroundColor(color);
	    }

	    @Override
	    public void NASA_teamNumber(int slot, String number)
	    {
		EditText team;
		switch(slot) {
		default:
		case 0:    	team = (EditText) findViewById(R.id.a_team); break;
		case 1:    	team = (EditText) findViewById(R.id.b_team); break;
		case 2:    	team = (EditText) findViewById(R.id.c_team); break;
		case 3:    	team = (EditText) findViewById(R.id.d_team); break;
		case 4:    	team = (EditText) findViewById(R.id.e_team); break;
		case 5:    	team = (EditText) findViewById(R.id.f_team); break;
		}
		team.setText(number);
	    }

	    @Override
	    public void NASA_dataTransmission(int slot, boolean finalChunk, String jsonData)
	    {
		CheckBox box = null;
		
		switch(slot) {
		default:
		case 0:    	box = (CheckBox) findViewById(R.id.a_hasData); break;
		case 1:    	box = (CheckBox) findViewById(R.id.b_hasData); break;
		case 2:    	box = (CheckBox) findViewById(R.id.c_hasData); break;
		case 3:    	box = (CheckBox) findViewById(R.id.d_hasData); break;
		case 4:    	box = (CheckBox) findViewById(R.id.e_hasData); break;
		case 5:    	box = (CheckBox) findViewById(R.id.f_hasData); break;
		}
		box.setChecked(finalChunk);
	    }

	    @Override
	    public void NASA_dataUploadStatus(int slot, boolean success)
	    {
		CheckBox box = null;
		
		switch(slot) {
		default:
		case 0:    	box = (CheckBox) findViewById(R.id.a_hasData); break;
		case 1:    	box = (CheckBox) findViewById(R.id.b_hasData); break;
		case 2:    	box = (CheckBox) findViewById(R.id.c_hasData); break;
		case 3:    	box = (CheckBox) findViewById(R.id.d_hasData); break;
		case 4:    	box = (CheckBox) findViewById(R.id.e_hasData); break;
		case 5:    	box = (CheckBox) findViewById(R.id.f_hasData); break;
		}

		if(success) {
		    box.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#00CC00")));
		} else{
		    box.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#CC0000")));
		}
		    
		Log.d("NASA_BLE","upload status called with slot " + slot + " and success " + success);
	    }

	    @Override
	    public void NASA_contributorName(int slot, String contributorName)
	    {
		EditText name;
		switch(slot) {
		default:
		case 0:    	name = (EditText) findViewById(R.id.a_name); break;
		case 1:    	name = (EditText) findViewById(R.id.b_name); break;
		case 2:    	name = (EditText) findViewById(R.id.c_name); break;
		case 3:    	name = (EditText) findViewById(R.id.d_name); break;
		case 4:    	name = (EditText) findViewById(R.id.e_name); break;
		case 5:    	name = (EditText) findViewById(R.id.f_name); break;
		}
		name.setText(contributorName);
	    }
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nasa);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

	onSwitch = (Switch) findViewById(R.id.appOn);

	ble = new NASA_BLE(this,bleCallbacks);

	connectionReportHandler = new Handler();
	startConnectionReportTask();			// update the connection count periodically

	onSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    Log.i("NASA","Change to - " + isChecked);
		    if(isChecked) {
			ble.startServer();
		    } else {
			ble.stopServer();
		    }
		}
	    });

	// monitor the next match button

	nextMatchButton = (Button) findViewById(R.id.nextMatch);
	nextMatchButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    match++;
		    ble.matchUpdateContributors();
		}
	    });

	// monitor the transmit button

	transmitButton = (Button) findViewById(R.id.transmit);
	transmitButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    ble.transmitContributors();
		}
	    });

	// monitor the start button

	startButton = (Button) findViewById(R.id.start);
	startButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    ble.startContributors();
		}
	    });

	// monitor the stop button

	stopButton = (Button) findViewById(R.id.stop);
	stopButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    ble.stopContributors();
		}
	    });
	
	// monitor the reset button

	resetButton = (Button) findViewById(R.id.reset);
	resetButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
		    ble.resetContributors();
		}
	    });
	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_nasa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionsReport()
    {
	int connections = ble.connections();
	TextView con = findViewById(R.id.connections);
	con.setText(Integer.toString(connections));
    }

    Runnable connectionReportRunnable = new Runnable() {
	    @Override
	    public void run() {
		try {
		    updateConnectionsReport();
		} finally {
		    connectionReportHandler.postDelayed(connectionReportRunnable,10000);
		}
	    }
	};
    
    private void startConnectionReportTask()
    {
	connectionReportRunnable.run();
    }
}
