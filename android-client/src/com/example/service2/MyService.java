package com.example.service2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends Service {
	
    //public static final String CHANNEL_ID = "ForegroundServiceChannel";
	Camera camera = null;
	TextToSpeech t1;

    @Override
    public void onCreate() {
        super.onCreate();

		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int frontCamera = 1;
        int backCamera= 0;
        Camera.getCameraInfo(backCamera, cameraInfo);
        try {
            camera = Camera.open(backCamera);
        } catch (RuntimeException e) {
            Log.d("kkkk","Camera not available: " + 1);
            camera = null;
            //e.printStackTrace();
        }
        Camera.Parameters params = camera.getParameters();
        // change the orientation of the camera
		if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
			params.set("orientation", "portrait");
			camera.setDisplayOrientation(90);
			params.setRotation(90);
		}
		else{
			params.set("orientation","landscape");
			camera.setDisplayOrientation(0);
			params.setRotation(0);	
		}
		// change the picture size
		List<Camera.Size> sizes = params.getSupportedPictureSizes();
		int max =  921600, index = 0;
		for (int i = 0; i < sizes.size(); i++){
		     Size s = sizes.get(i);
		     int size = s.height * s.width;
		     if (size <= max) {
		         index = i;
		         //max = size;
		     }
		}
		Camera.Size cameraSize;
		cameraSize = sizes.get(sizes.size() / 2);
		params.setPictureSize(cameraSize.width, cameraSize.height);
		camera.setParameters(params);
		try {
        	if (null == camera) {
                Log.d("kkkk","Could not get camera instance");
            } 
        	else {
                Log.d("kkkk","Got the camera, creating the dummy surface texture");
                 try {
                 	camera.setPreviewTexture(new SurfaceTexture(0));
                    camera.startPreview();
                } catch (Exception e) {
                    Log.d("kkkk","Could not set the surface preview texture");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            camera.release();
        }
		
		// -- --------------------------------------------------------------------------
		
		t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
	         @Override
	         public void onInit(int status) {
	            if(status != TextToSpeech.ERROR) {
	               t1.setLanguage(Locale.UK);
	            }
	         }
	      });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        //createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("My Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread
        
        camera.takePicture(null, null, mPictureCallback);

        return START_STICKY;
    }
 
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){
		
		@Override
		public void onPictureTaken(byte[] data, Camera arg1) {
			
			try {
	  			Thread.sleep(1000);
	  		} catch (InterruptedException e) {
	  			// TODO Auto-generated catch block
	  		 	e.printStackTrace();
	  		}
			Send send = new Send();
			send.execute(data);

                camera.startPreview();

		}
	};
    
	//Main Actions - Asynchronous
  	class Send extends AsyncTask<byte[], Void, Void> {
  		
  	    Socket s; //Socket Variable
  	    OutputStream dos;
  		DataInputStream dis;
  		String response = "";
  	    
  	    @Override
  	    protected Void doInBackground(byte[]... voids){
  	    	
  	    	byte[] data = voids[0];
  	    	
  	        try {
  	        	s = new Socket("192.168.1.78",8080);
  	            
  	            // writing to server
  				dos = s.getOutputStream();
  				dos.write(data);
  				dos.flush();
  				//dos.close();
  				s.shutdownOutput();
  				

  				// reading from server
  				dis = new DataInputStream (s.getInputStream() );
                response = dis.readLine();
  				s.close();

  	        } catch (IOException e) {
  	            e.printStackTrace();
  	        }
  	        return null;
  	    }
  	    
  	  protected void onPostExecute(Void result) {
  		  t1.speak(response, TextToSpeech.QUEUE_FLUSH, null);
  		  try {
  			  camera.takePicture(null, null, mPictureCallback);
  		  }
  		  catch(Exception e){
  			  e.printStackTrace();
  		  }
  		  super.onPostExecute(result);
	  }
  	}
	

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        camera.stopPreview();
		camera.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }*/
}
