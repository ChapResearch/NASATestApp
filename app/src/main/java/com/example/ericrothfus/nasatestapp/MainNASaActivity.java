package com.example.ericrothfus.nasatestapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Switch;

public class MainNASaActivity extends AppCompatActivity {

    Switch onSwitch;

    NASA_BLE ble;

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
	    public void NASA_dataTransmission(int slot, String jsonData)
	    {

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

	onSwitch = (Switch) findViewById(R.id.appOn);

	ble = new NASA_BLE(this);
	
	onSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    Log.i("NASA","Change to - " + isChecked);
		    if(isChecked) {
			ble.startServer(bleCallbacks);
		    } else {
			ble.stopServer();
		    }
		}
	    });

	// use the currently defined color setting routine to set grey for
	// all of the color indicators

	//	for(int i=0; i < 6; i++) {
	//	    bleCallbacks.NASA_teamColor(i,Color.LTGRAY);
	//	}
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

}
