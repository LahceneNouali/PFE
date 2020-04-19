package com.example.mycamera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback{

	Camera camera;
	SurfaceHolder holder;
	
	public ShowCamera(Context context, Camera camera) {
		
		super(context);
		this.camera = camera;
		holder = getHolder();
		holder.addCallback(this);
	
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		
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
		
		int max = 0;
		int index = 0;

		for (int i = 0; i < sizes.size(); i++){
		     Size s = sizes.get(i);
		     int size = s.height * s.width;
		     if (size > max) {
		         index = i;
		         max = size;
		     }
		}
		
		params.setPictureSize(sizes.get(index).width, sizes.get(index).height);
		
		camera.setParameters(params);
		
		try {
			
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		
		camera.stopPreview();
		camera.release();
		
	}

}
