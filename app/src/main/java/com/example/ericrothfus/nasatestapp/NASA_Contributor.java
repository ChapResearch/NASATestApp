package com.example.ericrothfus.nasatestapp;

//
// NASA_Contributor - one of the remote devices that contribute data to the
//                    controller.  There are normally 6, but this class doesn't
//                    know that.
//

import android.bluetooth.BluetoothDevice;

import java.util.Date;

public class NASA_Contributor {
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
    private boolean connected;		// true if connected
    public String data;			// last transmitted data (should be cleared after upload)
    public int color;

    public static final int timeout = 60;	// 60 second timeout before a slot is considered open (if needed)

    public NASA_Contributor()
    {
	connected = false;
	timestamp = 0;
	device = null;
	teamNumber = null;
	userName = null;
	color = 0;
    }

    public static int lookup(NASA_Contributor[] contributors, BluetoothDevice target)
    {
	int i;

	for(i=0; i < contributors.length; i++) {
	    //	    if(contributors[i].connected) {
		if(contributors[i].device.equals(target)){
		    return(i);
		}
		//	    }
	}

	return(-1);
    }
	
    //
    // slotAvailable() - checks to see if this slot is available. This means that either
    //			 we are not connected in this slot, or the last guy in this
    //			 slot
    public boolean slotAvailable(BluetoothDevice newDevice)
    {
	if(!connected) {
	    return(true);
	}

	// if the same device is reconnecting, then that's ok
	
	if(newDevice.equals(device)) {
	    return(true);
	}

	// if the old owner hasn't transmitted in awhile
	
	Date date = new Date();
	int now = (int)(date.getTime()/1000);
	
	if(now-timeout > timestamp) {
	    return(true);
	}

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
    }

    public void setUserName(String incomingUserName)
    {
	userName = incomingUserName;
    }
	
    public void setTeamColor(int incomingColor)
    {
	color = incomingColor;
    }

    public void disconnect()
    {
	connected = false;
    }
    
}
