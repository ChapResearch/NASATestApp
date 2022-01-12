package com.example.ericrothfus.nasatestapp;

import android.bluetooth.BluetoothDevice;

import java.util.Date;

//
// NASA_Contributor - one of the remote devices that contribute data to the
//                    controller.  There are normally 6, but this class doesn't
//                    know that.
//

public class NASA_Contributor {

    private static final String TAG = "NASA_BLE!";

    // whats in here?
    //  - BLE device
    //	- timestamp of latest transmission
    //  - blob for latest transmission
    //  - name of contributor
    //  - connection status (can also be inferred by lack of transmission)

    public int timestamp;		// last contact by this contributor
    public BluetoothDevice device;	// last device of this contributor
    public String userName;		// name of contributor
    public String teamNumber;		// team number of robot
    public String match;		// match number
    public boolean connected;		// true if connected
    public String data;			// last transmitted data (should be cleared after upload)
    public boolean hasData;		// true if data is valid (and not previously transmitted successfully)
    public int color;

    private NASA_DB nasaDB;

    private NASA_BLE_Interface NASAcallbacks;

    //    public static final int timeout = 60;	// 60 second timeout before a slot is considered open (if needed)
    public static final int timeout = 0;	// disable the timeout for now

    public NASA_Contributor(NASA_BLE_Interface callbacks)
    {
	NASAcallbacks = callbacks;

	nasaDB = new NASA_DB(NASAcallbacks);

	connected = false;
	timestamp = 0;
	device = null;
	teamNumber = null;
	userName = null;
	color = 0;
	data = new String();		// need an empty string to start
	hasData = false;
    }

    public static int lookup(NASA_Contributor[] contributors, BluetoothDevice target)
    {
	int i;

	// this loop used to have a lead-in of "if(contributors[i].connected)"
	//   but that was removed to (hopefully) allow stale connections to be
	//   removed
	
	for(i=0; i < contributors.length; i++) {
	    if(contributors[i].connected) {
		if(contributors[i].device.equals(target)){
		    return(i);
		}
	    }
	}

	return(-1);
    }
	
    //
    // slotAvailable() - checks to see if this slot is available. This means that either
    //			 we are not connected in this slot, or the last guy in this
    //			 slot
    public boolean slotAvailable(BluetoothDevice newDevice)
    {
	// if there is nothing connected, then that's ok
	
	if(!connected) {
	    return(true);
	}

	// if the same device is reconnecting, then that's ok, too
	
	if(newDevice.equals(device)) {
	    return(true);
	}

	// if the old owner hasn't transmitted in awhile
	//   and we're running in timeout mode

	if(timeout != 0) {
	    Date date = new Date();
	    int now = (int)(date.getTime()/1000);
	
	    if(now-timeout > timestamp) {
		return(true);
	    }
	}

	// otherwise, you can't have the slot!
	
	return(false);
    }

    public void slotClaim(BluetoothDevice newDevice)
    {
	connected = true;
	
	device = newDevice;

	slotRefresh();
    }

    public void slotRefresh()
    {
	Date date = new Date();
	timestamp = (int)(date.getTime()/1000);
    }

    public void setTeamNumber(String incomingTeamNumber)
    {
	teamNumber = incomingTeamNumber;
	slotRefresh();
    }

    public void setUserName(String incomingUserName)
    {
	userName = incomingUserName;
	slotRefresh();
    }
	
    public void setTeamColor(int incomingColor)
    {
	color = incomingColor;
	slotRefresh();
    }

    public void disconnect()
    {
	connected = false;
    }

    //
    // addData() - adds data to the data buffer. If final is false, then the data
    //             isn't marked (yet) as fully received.  If hasData is true, this
    //             means that the previous data had been marked final, so new data
    //		   is on its way in.  So the old data is cleared.
    //
    //	       TODO - using StringBuffer would probably be much more efficient
    //
    public void addData(String incomingData, boolean finalChunk)
    {
	if(hasData) {
	    // if there is old data, replace it
	    data = incomingData;
	} else {
	    // if there isn't old data, assume that we are simply adding to it
	    data = data + incomingData;
	}
	
	hasData = finalChunk;	// note whether it was final
	slotRefresh();
    }

    //
    // send() - sends this contributor's data up to the DB. The "slot" is there to
    //          provide easier success/failure information.
    //
    public void send(int slot, String year, String competition, String match)
    {
	if(hasData) {
	    nasaDB.send(slot,year,teamNumber,competition,match,data);
	}
    }
    
}
