//
// NASA_DB.java
//
//   Implements the NASA interface to the Firebase DB.
//
//   IMPLEMENTATION 1 - HTTP
//     The goal is to provide a simple mechanism to upload the contributor
//     data to the database. The caller starts the action by pressing
//     on the transmit/send button, which kicks off the async process
//     for transmission. The caller then gets notifications about the
//     success/failure of the transmission asyncronously. Or at least is
//     supposed to.
//
//     The code for posting data was found on stack overflow, with a link
//     to: http://hmkcode.com/android-send-json-data-to-server/
//
//  IMPLEMENTATION 2 - Firebase Direct
//     This has been changed to call firebase directly. Note that the password
//     sould be prompted, as opposed to (yikes) hardwired.
//

package com.example.ericrothfus.nasatestapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// firebase changes - 4/10/19


public class NASA_DB {
    private static final String TAG = "NASA_BLE!";

    static private final String defaultEmail = "nasascoutingapp@gmail.com";
    static private final String defaultPassword = "2468Appreciate";

    static private DatabaseReference mDatabase;
    static private FirebaseAuth mAuth = null;

    private NASA_BLE_Interface NASAcallbacks;
    
    public NASA_DB(NASA_BLE_Interface callbacks)
    {
	NASAcallbacks = callbacks;
	
	// When the first object is constructed, go ahead and login

	if(mAuth == null) {
	    login(defaultEmail,defaultPassword);
	}
    }

    // TODO - expose this login to the upper level NASA_BLE interface
    // TODO - expand the callbacks to include "login success/failure"
    // TODO - add connection monitoring for the DB (needs '.info/connected')
    // TODO - expand the callbacks to include "connection up/down"

    public void login(String email,String password)
    {
	Log.d(TAG,"NASA_DB - logging in");

	// NOTE - that this persistence call must be first
	FirebaseDatabase.getInstance().setPersistenceEnabled(true);

	mDatabase = FirebaseDatabase.getInstance().getReference();
	mAuth = FirebaseAuth.getInstance();

	mAuth.signInWithEmailAndPassword(email, password)
	    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
		    @Override
		    public void onComplete(Task<AuthResult> task) {

			Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());

			// TODO - this should call a "new" NASA callback for DB logged in
			if (task.isSuccessful()) {
			    Log.d(TAG,"We're in");
			} else {
			    Log.d(TAG,"Login failed:" + task.getException().getMessage());
			}
		    }
		});
    }

    public void send(final int slot, String year, String team, String competition, String match, String data)
    {
	if(year == null || team == null || competition == null || match == null) {
	    new Handler(Looper.getMainLooper()).post(new Runnable(){
		    @Override
		    public void run() {
			NASAcallbacks.NASA_dataUploadStatus(slot,false);
		    }
		});
	    return;
	}
	    
	Log.d(TAG,"Sending: " +
	      '/' + year +
	      '/' + team +
	      '/' + competition +
	      '/' + match);

	JSONObject recordData;
	Map<String,Object> jsonMap;
	      
	try {
	    recordData = new JSONObject(data);	// assumed already in JSON format
	    jsonMap = jsonToMap(recordData);
	} catch (JSONException e) {
	    Log.d(TAG,"Exception in json parsing");
	    new Handler(Looper.getMainLooper()).post(new Runnable(){
		    @Override
		    public void run() {
			NASAcallbacks.NASA_dataUploadStatus(slot,false);
		    }
		});
	    return;
	}

	FirebaseDatabase.getInstance().getReference()
	    .child(year).child(team).child(competition).child(match)
	    .setValue(jsonMap)
	    .addOnSuccessListener(new OnSuccessListener<Void>() {
		    @Override
		    public void onSuccess(Void aVoid) {
			new Handler(Looper.getMainLooper()).post(new Runnable(){
				@Override
				public void run() {
				    NASAcallbacks.NASA_dataUploadStatus(slot,true);
				}
			    });
		    }
		})
	    .addOnFailureListener(new OnFailureListener() {
		    @Override
		    public void onFailure(Exception e) {
			new Handler(Looper.getMainLooper()).post(new Runnable(){
				@Override
				public void run() {
				    NASAcallbacks.NASA_dataUploadStatus(slot,false);
				}
			    });
		    }
		});
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
	Map<String, Object> retMap = new HashMap<String, Object>();

	if(json != JSONObject.NULL) {
	    retMap = toMap(json);
	}
	return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
	Map<String, Object> map = new HashMap<String, Object>();

	Iterator<String> keysItr = object.keys();
	while(keysItr.hasNext()) {
	    String key = keysItr.next();
	    Object value = object.get(key);

	    if(value instanceof JSONArray) {
		value = toList((JSONArray) value);
	    }

	    else if(value instanceof JSONObject) {
		value = toMap((JSONObject) value);
	    }
	    map.put(key, value);
	}
	return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
	List<Object> list = new ArrayList<Object>();
	for(int i = 0; i < array.length(); i++) {
	    Object value = array.get(i);
	    if(value instanceof JSONArray) {
		value = toList((JSONArray) value);
	    }

	    else if(value instanceof JSONObject) {
		value = toMap((JSONObject) value);
	    }
	    list.add(value);
	}
	return list;
    }

}

// redeclared this "private" so it can live out its life here in this file

class NASA_DB_HTTP {
    private static final String TAG = "NASA_BLE!";

    static private String targetURL = "https://us-central1-nasa-7a363.cloudfunctions.net/catcher";

    public NASA_DB_HTTP()
    {
    }

    public void send(String year, String team, String competition, String match, String data)
    {
	// calls _send() below
	new HttpAsyncTask().execute(year,team,competition,match,data);
    }

    private String _send(String... params)
    {
	String json = "";
	
	try {
	    JSONObject postData = new JSONObject();

	    postData.put("year",params[0]);
	    postData.put("team_number",params[1]);
	    postData.put("competition",params[2]);
	    postData.put("match",params[3]);

	    JSONObject recordData = new JSONObject(params[4]);	// assumed already is JSON format

	    postData.put("record",recordData);

	    json = postData.toString();

	} catch (JSONException e) {
	    // TODO - so what to do?
	}

	// now we have the json data composed, time to set-up network

        InputStream inputStream = null;
        String result = "";
	
	try {

	    URL url = new URL(targetURL);

	    HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();

	    httpConn.setRequestMethod("POST");
	    httpConn.setRequestProperty("Content-Type","application/json;charset=utf-8");

	    OutputStream outputStream = httpConn.getOutputStream();
	    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");
	    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

	    bufferedWriter.write(json);
	    bufferedWriter.flush();
	    bufferedWriter.close();
	    outputStream.close();

	    httpConn.connect();

	    result = httpConn.getResponseMessage()+"";

	} catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

	return(result);

    }

    //
    // isConnected() - returns true if we are connected to the network
    //
    //    public boolean isConnected()
    //    {
    //        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
    //	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    //	if (networkInfo != null && networkInfo.isConnected()) {
    //	    return true;
    //	} else {
    //	    return false;
    //	}
    //    }
    

    // The POST is done async to allow other things to happen
    
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
	    Log.v(TAG, "backgrounding");
            return _send(strings);
        }

        // onPostExecute displays the results of the AsyncTask.
	//        @Override
	//        protected void onPostExecute(String result) {
	//            Toast.makeText(getApplicationContext(), "Data Sent!", Toast.LENGTH_LONG).show();
	//       }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
