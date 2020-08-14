package com.example.viguide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button btnStartService, btnStopService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // btnStartService = (Button) findViewById(R.id.buttonStartService);
        btnStopService = (Button) findViewById(R.id.buttonStopService);

        btnStopService.setOnClickListener(new View.OnClickListener() {
        	
            @Override
            public void onClick(View v) {
            	if(isMyServiceRunning()){
            		stopService();
            	}
            	quitApp(v);
            }
        });
        
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
       
        startService();
        
    }

    public void startService() {
    	if(checkpermissions(this)){
        	if(!isMyServiceRunning()){
        	    Intent serviceIntent = new Intent(this, MainService.class);
        	    serviceIntent.putExtra("inputExtra", "Foreground Service");
        	    startService(new Intent(this, MainService.class)); 
        	}
        }else{
        	finish();
    	}
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, MainService.class);
        stopService(serviceIntent);
    }
    
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    public static Boolean checkpermissions(Activity activity) {
    	
    	PackageManager mPackageManager = activity.getPackageManager();
    	int hasPermStorage = mPackageManager.checkPermission(android.Manifest.permission.CAMERA, activity.getPackageName());

    	if (hasPermStorage != PackageManager.PERMISSION_GRANTED) {
    		// do stuff
    		Toast.makeText(activity.getApplicationContext(), "NO CAMERA PERMISSION", Toast.LENGTH_LONG).show();
    	    return false;
    	} else if (hasPermStorage == PackageManager.PERMISSION_GRANTED) {
    		// do stuff
    		// Toast.makeText(activity.getApplicationContext(), "Has permission", Toast.LENGTH_LONG).show();
    		return true;
    	} else {
    		return false;
    	}
    }
    
   @SuppressLint("NewApi") public void quitApp(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}
