package com.example.mycamera;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Camera camera; 
	ShowCamera showCamera;
	FrameLayout frameLayout;
	
	EditText portNumber;
	TextView txtv;
	int port; 
	String response; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        portNumber = (EditText) findViewById(R.id.editText);
        txtv = (TextView) findViewById(R.id.textView);
        final Button click = (Button)findViewById(R.id.button);
        
        // open the camera
        camera = Camera.open();
        showCamera = new ShowCamera(this,camera);
        frameLayout.addView(showCamera);
        
        //Sets Up OnClick Listener For Button
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            	if(camera != null){ 
            		
        	    	port = Integer.valueOf(portNumber.getText().toString());
            		camera.takePicture(null, null, mPictureCallback);
            		click.setEnabled(false);
            		
            	}
            }
        });
    }
    
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){
		
		@Override
		public void onPictureTaken(byte[] data, Camera arg1) {
			
			    	Send send = new Send();
			    	send.execute(data);
					txtv.setText(response);
					
					camera.startPreview();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// take new picture
					camera.takePicture(null, null, mPictureCallback);
		}
	};
	
	//Main Actions - Asynchronous
	class Send extends AsyncTask<byte[], Void, Void> {
		
	    Socket s; //Socket Variable
	    DataOutputStream dos;
		DataInputStream dis;
	    
	    @Override
	    protected Void doInBackground(byte[]... voids){
	    	
	    	byte[] data = voids[0];
	    	
	        try {
	        	
	            s = new Socket("192.168.1.78",port);
	            // writing to server
				dos = new DataOutputStream(s.getOutputStream());
				dos.write(data);
				dos.flush();
				dos.close(); s.close();

				s = new Socket("192.168.1.78",port);
				// reading from server
				dis = new DataInputStream (s.getInputStream() );
                response = dis.readLine();
				s.close();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	    
	    protected void onPostExecute(String  result2){
	    	
	    	//txtv.setText(response);
	    	
	    }
	}
}


