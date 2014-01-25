package com.seeedstudio.workcard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.*;


public class MainActivity extends Activity implements SurfaceHolder.Callback{
	
	final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    static final int MEDIA_TYPE_IMAGE = 1;  
    static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;  
    static final int PICK_A_PICTURE = 101;  
    static final int CROP_A_PICTURE = 102;  
      
    Intent intent  = null;  
    
    Button btn_takePhoto, btn_pickImage, btn_ok;  
    EditText et_name, et_title, et_more;
    SurfaceView surfaceview1;
    SurfaceHolder sfh;
    
    Bitmap bmp = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
        btn_takePhoto=(Button)findViewById(R.id.buttonTakePhoto);  
        btn_pickImage=(Button)findViewById(R.id.buttonPickImage);  
        btn_ok = (Button)findViewById(R.id.buttonOK);
        
        et_name = (EditText)findViewById(R.id.editTextName);
        et_title = (EditText)findViewById(R.id.editTextTitle);
        et_more = (EditText)findViewById(R.id.editTextMore);
        
        surfaceview1 = (SurfaceView)findViewById(R.id.surfaceView1);
        surfaceview1.setZOrderOnTop(true);
        
        sfh = surfaceview1.getHolder();
        sfh.setFormat(PixelFormat.TRANSPARENT);
        sfh.setSizeFromLayout();
        sfh.addCallback(this);
        
        btn_takePhoto.setOnClickListener(new View.OnClickListener()  
        {         
            public void onClick(View v)  
            { 
            	
            	intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//create a intent to take picture  
            	if (isKitKat)
            	{
            		File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);    
            		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            		File picFile = new File(picDir.getPath() + File.separator + "IMAGE_"+ timeStamp + ".jpg"); 
            		
            		Uri fileUri = Uri.fromFile(picFile);
            		DataDevice dd = (DataDevice)getApplication();
            		dd.setImageUri(fileUri);
                    intent.putExtra( MediaStore.EXTRA_OUTPUT,  fileUri);
            	}
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);  
            }
        });  
        
        btn_pickImage.setOnClickListener(new View.OnClickListener()  
        {         
            public void onClick(View v)  
            { 
            	if (isKitKat)
            	{
            		intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            		
	            	startActivityForResult(intent, PICK_A_PICTURE);
            	}else
            	{
	            	intent = new Intent(Intent.ACTION_GET_CONTENT, null);
	            	intent.setType("image/*");
	            	intent.putExtra("crop", "true");
	            	intent.putExtra("aspectX", 1);
	            	intent.putExtra("aspectY", 1);
	            	intent.putExtra("outputX", 176);
	            	intent.putExtra("outputY", 176);
	            	intent.putExtra("scale", true);
	            	intent.putExtra("return-data", true);
	            	intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
	            	intent.putExtra("noFaceDetection", true); // no face detection
	            	startActivityForResult(intent, CROP_A_PICTURE);
            	}
            }
        });  
        
        btn_ok.setOnClickListener(new View.OnClickListener()  
        {         
            public void onClick(View v)  
            { 
            	if (bmp != null)
            	{
		        	DataDevice myApp  =  (DataDevice)getApplication();
		        	myApp.setBitmap(bmp); 
		        	
		        	intent = new Intent(MainActivity.this,SendActivity.class);
		        	
		        	intent.putExtra("name", et_name.getText().toString());
		            intent.putExtra("title", et_title.getText().toString());
		            intent.putExtra("more", et_more.getText().toString());
		            startActivity(intent); 
            	}
            }
        });  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void setupCropActivity(Uri imgUri, int code)
	{
		intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(imgUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 176);
		intent.putExtra("outputY", 176);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, code);
	}

	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
        Log.d("workcard", String.format("requestCode: %d,  resultCode: %d \r\n", requestCode, resultCode));  
        switch(requestCode)
        {
        case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
            if (resultCode == RESULT_OK) { 
            	Uri imgUri;
            	if (isKitKat)
            	{
            		DataDevice dd = (DataDevice)getApplication();
            		imgUri = dd.getImageUri();
            	}else
            		imgUri = data.getData();
            	
            	Log.d("workcard", String.format("uri: %s", imgUri.toString()));
            	
        		setupCropActivity(imgUri, CROP_A_PICTURE);
            }
            break;
        case PICK_A_PICTURE:
        	if (resultCode == RESULT_OK && data != null) {  
        		setupCropActivity(data.getData(), CROP_A_PICTURE);
        	}
        	break;
        case CROP_A_PICTURE:
        	if (resultCode == RESULT_OK) {  
        		Bitmap _bmp = data.getParcelableExtra("data");
        		int w = _bmp.getWidth();
        		float scale = 176.0f/w;
        		Matrix m = new Matrix();
        		m.postScale(scale, scale);
        		bmp = Bitmap.createBitmap(_bmp, 0, 0, w, _bmp.getHeight(), m, true);
        		Log.d("workcard", String.format("bmp2: w %d, h %d", bmp.getWidth(), bmp.getHeight()));
        		
        	}
        	break;
        default:
        	break;
        }
    }  
	
	private void drawBitmap(Bitmap _bmp)
	{
		if (_bmp.getByteCount() < 1)
			return;
		
		Log.d("workcard", String.format("bmp3: w %d, h %d", _bmp.getWidth(), _bmp.getHeight()));
		
		Rect sf_rect = sfh.getSurfaceFrame();
		int sf_w = sf_rect.width();
		int sf_h = sf_rect.height();
		
		int size = (sf_w/sf_h > 1.5)?(sf_h):(sf_w*2/3);
		
		Rect r1 = new Rect(0, 0, 176, 176);
		Rect r2 = new Rect(0, 0, size, size);
		r2.offset((sf_w-size)/2, (sf_h-size)/2);

		Canvas canvas = sfh.lockCanvas();
		canvas.drawBitmap(_bmp, r1, r2, null);
		sfh.unlockCanvasAndPost(canvas);
	}
	
	
	// 实现SurfaceHolder.Callback接口中的三个方法，都是在主线程中调用，而不是在绘制线程中调用的
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("workcard", "surfaceChanged");
		if (bmp != null)
			drawBitmap(bmp);		
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 启动线程。当这个方法调用时，说明Surface已经有效了
		//myThread.setRun(true);
		//myThread.start();
		Log.d("workcard", "created");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// 结束线程。当这个方法调用时，说明Surface即将要被销毁了
		//myThread.setRun(false);
		Log.d("workcard", "surfaceDestroyed");
	}

}
