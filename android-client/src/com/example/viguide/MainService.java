package com.example.viguide;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

@SuppressWarnings("deprecation")
public class MainService extends Service {
	
    //public static final String CHANNEL_ID = "ForegroundServiceChannel";
	Camera camera = null;
	TextToSpeech t1;

    @Override
    public void onCreate() {
        super.onCreate();

		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        // int frontCamera = 1;
        int backCamera = 0;
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
        params.set("orientation", "portrait");
        params.setRotation(90);
        
		// Check what resolutions are supported by your camera
		List<Size> sizes = params.getSupportedPictureSizes();

		// Iterate through all available resolutions and choose one
		// The chosen resolution will be stored in mSize
		Size mSize = null;
		for (Size size : sizes) {
		    // Log.i(TAG, "Available resolution: "+size.width+" "+size.height);
			if(size.width < 720)	break;
			mSize = size;
		}
		
		// Log.i(TAG, "Chosen resolution: "+mSize.width+" "+mSize.height);
		params.setPictureSize(mSize.width, mSize.height);
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
        // createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
        		0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Visually Impaired Guide")
                .setContentText(input)
                .setSmallIcon(R.drawable.baseline_linked_camera_black_24)
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
			
			/*try {
	  			Thread.sleep(1000);
	  		} catch (InterruptedException e) {
	  			// TODO Auto-generated catch block
	  		 	e.printStackTrace();
	  		}*/
			SendPicture send = new SendPicture();
			send.execute(data);
            camera.startPreview();

		}
	};
    
	//Main Actions - Asynchronous
  	class SendPicture extends AsyncTask<byte[], Void, Void> {
  		
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
        		t1.speak(response, TextToSpeech.QUEUE_FLUSH, null);
        		
  				s.close();

  	        } catch (IOException e) {
  	            e.printStackTrace();
  	        }
  	        return null;
  	    }
  	    
  	  protected void onPostExecute(Void result) {
  		  super.onPostExecute(result);
  		  try {
    		  camera.takePicture(null, null, mPictureCallback); 
  		  } catch (Exception e) {
  			  e.printStackTrace();
  		  }
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
