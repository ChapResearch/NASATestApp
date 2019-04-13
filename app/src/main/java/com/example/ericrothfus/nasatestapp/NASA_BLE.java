package com.example.ericrothfus.nasatestapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class NASA_BLE {

    private static final String TAG = "NASA_BLE!";

    private Context context;

    // the service itself has its UUID
    private static final UUID NASA_BLE_SERVICE_UUID = UUID.fromString("df85c229-277c-4070-97e3-abc1e134b6a1");

    // the general characterstics for talking to the controller
    private static final UUID NASA_BLE_CONTROLLER_NAME = UUID.fromString("d9867b1d-15dd-4f18-a134-3d8e4408fcff");
    private static final UUID NASA_BLE_CONTROLLER_PW = UUID.fromString("b1de1e91-6d8a-4b5c-8b06-2e64688d3fc9");
    private static final UUID NASA_BLE_CONTROLLER_MATCH = UUID.fromString("5eda1292-e156-11e8-9f32-f2801f1b9fd1");
    private static final UUID NASA_BLE_CONTROLLER_START = UUID.fromString("baa7db04-1e87-11e9-ab14-d663bd873d93");
    private static final UUID NASA_BLE_CONTROLLER_RESET = UUID.fromString("50c0ee7a-231d-11e9-ab14-d663bd873d93");

    // the individual characteristics for each contributor
    private UUID[] NASA_BLE_CONTRIBUTORS_READ = {
	/* A */ UUID.fromString("b5c1d1ae-eb68-42f2-bc5f-278902252ca9"),
	/* B */ UUID.fromString("02dad9d7-10c0-467f-9a64-9f2a380dc7bf"),
	/* C */ UUID.fromString("6a081497-98f2-44f6-b75c-e7c719866767"),
	/* D */ UUID.fromString("a6a99e2f-3b41-404c-a437-adf0aa3beb30"),
	/* E */ UUID.fromString("84f9047f-054e-4a34-92e7-a4ce528e8963"),
	/* F */ UUID.fromString("9646616b-2dfd-4f43-b0ea-febc1aa49b54")
    };

    private UUID[] NASA_BLE_CONTRIBUTORS_WRITE = {
	/* A */ UUID.fromString("1a17ae70-1d3f-4193-81c1-99a40cd219cf"),
	/* B */ UUID.fromString("61c15dc1-745f-4d25-bbdd-0518b5669180"),
	/* C */ UUID.fromString("0cabc681-2ec4-44f4-bb09-7f80c1856923"),
	/* D */ UUID.fromString("4d81b727-c4e0-4f76-a76a-386ed7050ff7"),
	/* E */ UUID.fromString("4ceb6d73-9cea-4813-8f3b-5c78170642e3"),
	/* F */ UUID.fromString("7f1d572e-cd58-49c5-b59e-647a3e89de29")
    };

    private NASA_Contributor[] contributors = new NASA_Contributor[6];

    private NASA_BLE_Interface NASAcallbacks;
    private BluetoothGattService mBluetoothGattService;
    private HashSet<BluetoothDevice> mBluetoothDevices;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private AdvertiseData mAdvData;
    private AdvertiseData mAdvScanResponse;
    private AdvertiseSettings mAdvSettings;
    private BluetoothLeAdvertiser mAdvertiser;

    private BluetoothGattServer nasaGATTserver;

    private BluetoothGattService nasaGATTservice;
    private BluetoothGattCharacteristic[] nasaGATTcharacteristicsREAD = new BluetoothGattCharacteristic[6];
    private BluetoothGattCharacteristic[] nasaGATTcharacteristicsWRITE = new BluetoothGattCharacteristic[6];
    
    private BluetoothGattCharacteristic nasaGATTcharacteristicsNAME,
	                                nasaGATTcharacteristicsPW,
	                                nasaGATTcharacteristicsMATCH,
	                                nasaGATTcharacteristicsSTART,
	                                nasaGATTcharacteristicsRESET;

    //
    // CONSTRUCTOR - When creating the NASA_BLE object, go ahead and define all of the
    //               services and descriptors.
    //
    public NASA_BLE(Context incomingContext,NASA_BLE_Interface callbacks)
    {
	context = incomingContext;
	NASAcallbacks = callbacks;
	
	for(int i=0; i < 6; i++) {
	    contributors[i] = new NASA_Contributor(NASAcallbacks);
	}
	
	_initialize();
	_defineCharacteristics();
	//	_defineAdvertisting();
	_defineService();
	_addCharacteristics();
    }

    public void _initialize()
    {
	mBluetoothDevices = new HashSet<>();
	mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
	mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    //
    // startContributors() - starts the clock of the contributors who are currently connected.
    //                       It is assumed that the contributors are listening for notifications on
    //                       the start characteristic. (See "stopContributors()" below)
    //
    public void startContributors()
    {
	for(int i=0; i < contributors.length; i++) {
	    if(contributors[i].connected) {
		nasaGATTcharacteristicsSTART.setValue("1");
		nasaGATTserver.notifyCharacteristicChanged(contributors[i].device,
							   nasaGATTcharacteristicsSTART,
							   false);
	    }
	}
    }

    //
    // stopContributors() - stops the clock of the contributors who are currently connected.
    //                       It is assumed that the contributors are listening for notifications on
    //                       the start characteristic. (See "startContributors()" above)
    //
    public void stopContributors()
    {
	for(int i=0; i < contributors.length; i++) {
	    if(contributors[i].connected) {
		nasaGATTcharacteristicsSTART.setValue("0");
		nasaGATTserver.notifyCharacteristicChanged(contributors[i].device,
							   nasaGATTcharacteristicsSTART,
							   false);
	    }
	}
    }
	
    //
    // resetContributors() - called when the UI wants to reset the contributors for the next match.
    //                       It is assumed that the contributors are listening for notifications.
    //                       NASA_BLE callbacks are executed to clear data.
    //
    public void resetContributors()
    {
	for(int i=0; i < contributors.length; i++) {
	    if(contributors[i].connected) {
		nasaGATTcharacteristicsRESET.setValue("1");
		nasaGATTserver.notifyCharacteristicChanged(contributors[i].device,
							   nasaGATTcharacteristicsRESET,
							   false);

		contributors[i].hasData = false;	// clear old data
		contributors[i].teamNumber = null;	// clear team number

		// the contributors will clear their team number and color, so go ahead and do it too
		// also, the hasData flag is cleared

		final int slot = i;
		new Handler(Looper.getMainLooper()).post(new Runnable(){
			@Override
			public void run() {
			    NASAcallbacks.NASA_teamNumber(slot,"");
			    NASAcallbacks.NASA_teamColor(slot,0);
			    NASAcallbacks.NASA_dataTransmission(slot,false,"");
			    NASAcallbacks.NASA_dataUploadStatus(slot,false);
			}
		    });
	    }
	}
    }
	
    //
    // matchUpdateContributors() - called when the UI wants to change the match number for the
    //				   contributors for the next match. It is assumed that the
    //				   contributors are listening for notifications on the match
    //				   number and that the match number was updated before calling this.
    //
    public void matchUpdateContributors()
    {
	for(int i=0; i < contributors.length; i++) {
	    if(contributors[i].connected) {
		Log.v(TAG, "match notifying slot " + i);
		nasaGATTcharacteristicsMATCH.setValue(NASAcallbacks.NASA_match());
		nasaGATTserver.notifyCharacteristicChanged(contributors[i].device,
							   nasaGATTcharacteristicsMATCH,
							   false);
	    }
	}
    }

    //
    // transmitContributors() - sends the data for each Contributor who has specified data, up to
    //                          the database. The callback will be called with
    //
    public void transmitContributors()
    {
	for(int i=0; i < contributors.length; i++) {
	    if(contributors[i].connected) {
		contributors[i].send(i,
				     NASAcallbacks.NASA_year(),
				     NASAcallbacks.NASA_competition(),
				     NASAcallbacks.NASA_match());
		Log.v(TAG, "transmit slot " + i);
	    }
	}
    }
	

    //
    // clearConnections() - this a brutal and unforgiving call! It will mercilessly kill all
    //                      current bluetooth connections with no respect to who/what started
    //                      it. So there!
    //
    public void clearConnections()
    {
	List<BluetoothDevice> connectedDevices =
	    mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT_SERVER);

	for(int i=0; i < connectedDevices.size(); i++) {
	    nasaGATTserver.cancelConnection(connectedDevices.get(i));
	}
    }

    public int connections()
    {
	return(mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT_SERVER).size());
    }
    
    public void stopServer()
    {
          nasaGATTserver.close();
    }
    
    public void startServer()
    {
	nasaGATTserver = mBluetoothManager.openGattServer(context, mGattServerCallback);

	// CLEAR ALL CONNECTIONS!  This SHOULD work, but as seen in many references on
	//   stack overflow - it doesn't.  Maybe it will some day.
	
	clearConnections();


	// Add a service for a total of three services (Generic Attribute and Generic Access
	// are present by default).
	nasaGATTserver.addService(nasaGATTservice);

	if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
	    mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
	    mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
	} else {
	    // TODO
	}
    }

    //
    // uploadContributorData() - kick-off the process of uploading contributor data. Only the
    //                           data that is "current" is uploaded.  That is, if the data
    //                           either hasn't been received or has already been uploaded,
    //                           then it will not be uploaded. This is true for each "slot"
    //                           of contributor data.
    //
    public void uploadContributorData()
    {
    	for(int i=0; i < contributors.length; i++) {
	    if(contributors[i].hasData) {
		// send it here
		// with a response that will call the UI for each slot (i)
		// if(goodresponse) {
		//     contributors[i].hasData = false;
		// }
	    }
	}
    }
	
    private void _defineService()
    {
	nasaGATTservice = new BluetoothGattService(NASA_BLE_SERVICE_UUID,BluetoothGattService.SERVICE_TYPE_PRIMARY);

	mAdvSettings = new AdvertiseSettings.Builder()
	    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
	    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
	    .setConnectable(true)
	    .build();
	mAdvData = new AdvertiseData.Builder()
	    .setIncludeTxPowerLevel(true)
	    .addServiceUuid(new ParcelUuid(NASA_BLE_SERVICE_UUID))
	    .build();
	mAdvScanResponse = new AdvertiseData.Builder()
	    .setIncludeDeviceName(true)
	    .build();
    }

    private void _defineCharacteristics()
    {
	int i;
	int readProps = BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY;
	int writeProps = BluetoothGattCharacteristic.PROPERTY_WRITE;

	for(i=0; i < 6; i++) {
	    nasaGATTcharacteristicsREAD[i] = new BluetoothGattCharacteristic(NASA_BLE_CONTRIBUTORS_READ[i], readProps,
									    BluetoothGattCharacteristic.PERMISSION_READ);
	    nasaGATTcharacteristicsWRITE[i] = new BluetoothGattCharacteristic(NASA_BLE_CONTRIBUTORS_WRITE[i], writeProps,
									    BluetoothGattCharacteristic.PERMISSION_WRITE);
	}

	nasaGATTcharacteristicsNAME = new BluetoothGattCharacteristic(NASA_BLE_CONTROLLER_NAME, readProps,
									    BluetoothGattCharacteristic.PERMISSION_READ);

	nasaGATTcharacteristicsPW = new BluetoothGattCharacteristic(NASA_BLE_CONTROLLER_PW, readProps,
									    BluetoothGattCharacteristic.PERMISSION_READ);

	nasaGATTcharacteristicsMATCH = new BluetoothGattCharacteristic(NASA_BLE_CONTROLLER_MATCH, readProps,
									    BluetoothGattCharacteristic.PERMISSION_READ);

	nasaGATTcharacteristicsSTART = new BluetoothGattCharacteristic(NASA_BLE_CONTROLLER_START, readProps,
									    BluetoothGattCharacteristic.PERMISSION_READ);

	nasaGATTcharacteristicsRESET = new BluetoothGattCharacteristic(NASA_BLE_CONTROLLER_RESET, readProps,
									    BluetoothGattCharacteristic.PERMISSION_READ);

	nasaGATTcharacteristicsMATCH.addDescriptor(getClientCharacteristicConfigurationDescriptor());
	nasaGATTcharacteristicsSTART.addDescriptor(getClientCharacteristicConfigurationDescriptor());
	nasaGATTcharacteristicsRESET.addDescriptor(getClientCharacteristicConfigurationDescriptor());

    }

    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	

    public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
	BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
									 CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
									 (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
	descriptor.setValue(new byte[]{0, 0});
	return descriptor;
  }

    
    //
    // _addCharacteristics() - after the service has been defined, add the previously defined
    // 			       characteristics to it.
    //
    private void _addCharacteristics()
    {
	int i;
	for(i=0; i < 6; i++) {
	    nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsREAD[i]);
	    nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsWRITE[i]);
	}
	nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsNAME);
	nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsPW);
	nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsMATCH);
	nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsSTART);
	nasaGATTservice.addCharacteristic(nasaGATTcharacteristicsRESET);
    }

    // This is the callback to process advertisting once the GATT server is started-up
    
    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

	    @Override
	    public void onStartFailure(int errorCode) {
		super.onStartFailure(errorCode);
		Log.e(TAG, "Not broadcasting: " + errorCode);
	    }

	    @Override
	    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
		super.onStartSuccess(settingsInEffect);
		Log.v(TAG, "Broadcasting");
	    }
	};

    // This creates a callback for the server, by using the abstract class/interface "BluetoothGattServerCallback"
    //   and defining an "anonymous class" that creates the callback for the GATT server

	private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

		@Override
		public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
			super.onConnectionStateChange(device, status, newState);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {
					mBluetoothDevices.add(device);
					Log.v(TAG, "Connected to device: " + device.getAddress());
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
					int target = NASA_Contributor.lookup(contributors,device);
					mBluetoothDevices.remove(device);
					if(target >= 0) {
					    Log.v(TAG, "Disconnected from device " + target);
					    contributors[target].disconnect();
					    final int slotdis = target;
					    new Handler(Looper.getMainLooper()).post(new Runnable(){
						    @Override
						    public void run() {
							NASAcallbacks.NASA_slotChange(slotdis,false);
						    }
						});
					} else {
					    Log.v(TAG, "Disconnected from un-tracked device");
					}
				}
			} else {
				mBluetoothDevices.remove(device);
				Log.e(TAG, "Error when connecting: " + status);
			}
		}


		//
		// onCharacteristicReadRequest() - process incoming read requests from the contributor, by calling
		//                                 the NASA server callbacks appropriately.
		//
		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device,            // the remote device
												int requestId,                // unique id of the request
												int offset,                    // offset into the value requested
												BluetoothGattCharacteristic characteristic    // characteristic to read
		) {
			super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

			Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());

			// we don't support offsetting into characteristics
			if (offset != 0) {
				nasaGATTserver.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset, null);
				return;
			} else {
				if (characteristic.getUuid().equals(NASA_BLE_CONTROLLER_NAME)) {
					characteristic.setValue(NASAcallbacks.NASA_controllerName());
				} else if (characteristic.getUuid().equals(NASA_BLE_CONTROLLER_PW)) {
					characteristic.setValue(NASAcallbacks.NASA_password());
				} else if (characteristic.getUuid().equals(NASA_BLE_CONTROLLER_MATCH)) {
					characteristic.setValue(NASAcallbacks.NASA_match());
				} else if (characteristic.getUuid().equals(NASA_BLE_CONTROLLER_START)) {
					characteristic.setValue("1");
				} else if (characteristic.getUuid().equals(NASA_BLE_CONTROLLER_RESET)) {
					characteristic.setValue("1");
				}
				nasaGATTserver.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
						offset, characteristic.getValue());
			}
		}

		@Override
		public void onNotificationSent(BluetoothDevice device, int status) {
			super.onNotificationSent(device, status);
			Log.v(TAG, "Notification sent. Status: " + status);
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device,
							 int requestId,
							 BluetoothGattCharacteristic characteristic,
							 boolean preparedWrite, boolean responseNeeded,
							 int offset, byte[] value)
		{
			super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
					responseNeeded, offset, value);

			// our contributors will write to their slot's write uuid, so we need to look-up
			// the contributor based upon the UUID being written to

			int i;
			for (i = 0; i < 6; i++) {
				if (characteristic.getUuid().equals(NASA_BLE_CONTRIBUTORS_WRITE[i])) {
					break;
				}
			}

			// we'll assume here that i is between 0 and 5 - that is, there are NO WRITES
			//   that are done on something other the ones above. But each write can be
			//   of the following types:
			//
			//       0x00 - slot claim - sent right after first connect
			//	 0x01 - keep alive - sent when client wants to continue with its slot
			//	 0x02 - team number follows - ascii characters (no need to treat as number)
			//	 0x03 - color follows - 0x00 no color - 0x01 blue - 0x02 red - 0x?? no color
			//	 0x04 - person name - ascii chacaters
			//       0x05 - data transmission - first byte is remaining packets AFTER this one,
			//		so the first of two packets has 0x01 as the first byte and the second
			//              packet has 0x00 as the byte.

			int status = BluetoothGatt.GATT_SUCCESS;
			int command = (int)(value[0]);
			byte[] data = Arrays.copyOfRange(value,1,value.length);

			// if the slot is not available, the fail on the write
			
			if (!contributors[i].slotAvailable(device)) {
			    status = BluetoothGatt.GATT_WRITE_NOT_PERMITTED;
			} else {

			    switch(command) {

			    case 0x00:	// slot claim
				Log.v(TAG, "Got a slot claim");
				contributors[i].slotClaim(device);
				final int i2 = i;
				new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run() {
					    NASAcallbacks.NASA_slotChange(i2,true);
					}
				    });
				
				break;

			    case 0x01:	// keep alive
				Log.v(TAG, "Got a keep alive");
				int count = mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT_SERVER).size();
				Log.v(TAG, "Count is " + count);
				contributors[i].slotRefresh();
				break;
			    
			    case 0x02:	// team number incoming
				final String teamNumber = new String(data);
				Log.v(TAG, "Got a number of \"" + teamNumber + "\"");
				contributors[i].setTeamNumber(teamNumber);
				final int i3 = i;
				new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run() {
					    NASAcallbacks.NASA_teamNumber(i3,teamNumber);
					}
				    });
				break;

			    case 0x03:	// color coming (0x00 no color, 0x01 blue, 0x02 red, else no color)
				final int color = (int)data[0];
				Log.v(TAG, "Got a color of \"" + ((color==1)?"blue":((color==2)?"red":"no color")) + "\"");
				contributors[i].setTeamColor(color);
				final int i4 = i;
				new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run() {
					    NASAcallbacks.NASA_teamColor(i4,color);
					}
				    });
				break;
			    
			    case 0x04:	// user name
				final String userName = new String(data);
				Log.v(TAG, "Got a name of \"" + userName + "\"");
				contributors[i].setUserName(userName);
				final int i5 = i;
				new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run() {
					    NASAcallbacks.NASA_contributorName(i5,userName);
					}
				    });
				break;

				// data transmission - a very simple approach is used, each data packet
				// coming in has a one byte leader that says whether this is the FINAL
				// packet or not. Zero is not final, otherwise final.
				
			    case 0x05:	// data transmission - packet number first byte
				final boolean finalChunk = (((int)data[0]) != 0);
				byte[] restOfData = Arrays.copyOfRange(data,1,data.length);
				final String dataChunk = new String(restOfData);
				Log.v(TAG, "Got a data packet with data \"" + dataChunk + "\", final is: " + finalChunk);
				final int i6 = i;
				contributors[i].addData(dataChunk,finalChunk);
				new Handler(Looper.getMainLooper()).post(new Runnable(){
					@Override
					public void run() {
					    NASAcallbacks.NASA_dataTransmission(i6,finalChunk,dataChunk);
					}
				    });
				
				break;
			    }
			}

			Log.v(TAG, "Characteristic Write (command " + command + ") request on slot " + i + " at " + contributors[i].timestamp);

			// TODO - maybe respond with owner upon failure?

			if (responseNeeded) {
				nasaGATTserver.sendResponse(device, requestId, status,
						/* No need to respond with an offset */ 0,
						/* No need to respond with a value */ null);
			}
		}

		@Override
		public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
											int offset, BluetoothGattDescriptor descriptor) {
			super.onDescriptorReadRequest(device, requestId, offset, descriptor);
			Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
			if (offset != 0) {
				nasaGATTserver.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
						/* value (optional) */ null);
				return;
			}
			nasaGATTserver.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
					descriptor.getValue());
		}

		@Override
		public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
											 BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
											 int offset,
											 byte[] value) {
			super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
					offset, value);
			Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid());
			int status = BluetoothGatt.GATT_SUCCESS;
			if (responseNeeded) {
				nasaGATTserver.sendResponse(device, requestId, status,
						/* No need to respond with offset */ 0,
						/* No need to respond with a value */ null);
			}
		}
	};

}
