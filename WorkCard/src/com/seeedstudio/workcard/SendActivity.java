package com.seeedstudio.workcard;

import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.text.Layout.Alignment;
import android.text.*;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.graphics.*;
import android.graphics.Paint.FontMetrics;

public class SendActivity extends Activity {
	
	PowerManager.WakeLock wakeLock;  
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	
	private long cpt = 0;
	byte[] GetSystemInfoAnswer = null;
	
	private byte[] bufferFile = null;;
	private int blocksToWrite = 0;
	private byte[] WriteSingleBlockAnswer = null;
	private byte [] addressStart = null;
	private byte[] dataToWrite = new byte[4];
	
	
	ImageView imageview1;
	ProgressBar bar1;
	TextView textview1;
	
	Bitmap bmp_final, bmp_display;
	Canvas canvas;
	
	Boolean img_ready = false;
	String str_name, str_title, str_more;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");  
		
		img_ready = false;
		
		imageview1 = (ImageView)findViewById(R.id.imageView1);
		bar1 = (ProgressBar)findViewById(R.id.progressBar1);
		textview1 = (TextView)findViewById(R.id.textView1);
				
		bmp_final = Bitmap.createBitmap(264, 176, Bitmap.Config.ARGB_8888);
		bmp_final.eraseColor(Color.WHITE);
		canvas = new Canvas(bmp_final);
		
		Intent intent = getIntent();
        
		if(intent!=null)
        {
			DataDevice myApp  =  (DataDevice)getApplication();
			Bitmap getBmp = myApp.getBitmap();
			
            str_name = intent.getStringExtra("name").isEmpty()?"Name":intent.getStringExtra("name");
            str_title = intent.getStringExtra("title").isEmpty()?"Title":intent.getStringExtra("title");
            str_more = intent.getStringExtra("more").isEmpty()?"More informations...":intent.getStringExtra("more");
            
            //imageview1.setImageBitmap(getBmp);
            
            canvas.drawBitmap(getBmp, 0, 0, null);
            TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Color.BLACK);
            p.setTextSize(22);
            p.setAntiAlias(true);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            FontMetrics fontMetrics = p.getFontMetrics();  
            float fontH = fontMetrics.descent - fontMetrics.ascent;
            float base = fontH + 10;
            
            canvas.drawText(str_name, 180, base, p);
            canvas.drawText(str_title, 180, base + fontH, p);
            
            p.setTextSize(16);
            fontMetrics = p.getFontMetrics();  
            StaticLayout layout = new StaticLayout(str_more,p,80,Alignment.ALIGN_NORMAL,1.0F,0.0F,true);
            canvas.translate(180,base + fontH + 10);
            layout.draw(canvas);

            bmp_final = this.convertGreyImg(bmp_final);
            //imageview1.setImageBitmap(bmp_final);
            
            int screenWidth  = getWindowManager().getDefaultDisplay().getWidth();
            float scale = (screenWidth-40.0f)/264.0f;
            Matrix mtx = new Matrix();
            mtx.postScale(scale, scale);
            bmp_display = Bitmap.createBitmap(bmp_final, 0, 0, 264, 176, mtx, true);
            imageview1.setImageBitmap(bmp_display);
            
            img_ready = true;
        } 
		
		// Check for available NFC Adapter
    	PackageManager pktm = getPackageManager();
    	if(!pktm.hasSystemFeature(PackageManager.FEATURE_NFC))
    	{
    		textview1.setText(R.string.no_nfc);
    	}
    	else
    	{
	        mAdapter = NfcAdapter.getDefaultAdapter(this);
	        if (mAdapter.isEnabled())
	        {		        
		        mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		        mFilters = new IntentFilter[] {ndef,};
		        mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
    		}
	        else
	        {
	        	textview1.setText(R.string.nfc_disabled);
	        }
    	}
	}
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	// TODO Auto-generated method stub
    	super.onNewIntent(intent);
    	String action = intent.getAction();
    	if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))    	
    	{
	    	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	
	    	DataDevice dataDevice = (DataDevice)getApplication();
	    	dataDevice.setCurrentTag(tagFromIntent);
			
			byte[] GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(tagFromIntent,(DataDevice)getApplication());
			
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				bar1.setProgress(0);
	    		bar1.setVisibility(ProgressBar.VISIBLE);
	    		new SendTask().execute();	
	    	}
			else
			{
				return;
			}
    	}
    }
	
	@Override
    protected void onResume() 
    {
    	// TODO Auto-generated method stub
    	super.onResume();
    	//Used for DEBUG : Log.v("NFCappsActivity.java", "ON RESUME NFC APPS ACTIVITY");
    	mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    	mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);    	 
    
    	wakeLock.acquire();
    	
    	Log.d("workcard", "onResume");
		
    }
        
    @Override
    protected void onPause() 
    {
    	// TODO Auto-generated method stub
    	//Used for DEBUG : Log.v("NFCappsActivity.java", "ON PAUSE NFC APPS ACTIVITY");
    	super.onPause();
    	cpt = 500;
    	mAdapter.disableForegroundDispatch(this);   

		if (wakeLock != null) {  
            wakeLock.release();  
        }  
		
		Log.d("workcard", "onPause");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send, menu);
		return true;
	}
	
	
	/**
     * 将彩色图转换为灰度图
     * @param img 位图
     * @return  返回转换好的位图
     */ 
    public Bitmap convertGreyImg(Bitmap img) { 
        int width = img.getWidth();         //获取位图的宽  
        int height = img.getHeight();       //获取位图的高  

        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组  
        bufferFile = new byte[width * height /8];

        img.getPixels(pixels, 0, width, 0, 0, width, height); 
        int alpha = 0xFF << 24;  
        for(int i = 0; i < height; i++)  { 
            for(int j = 0; j < width; j++) { 
            	int index = width * i + j;
                int grey = pixels[index]; 

                int red = ((grey  & 0x00FF0000 ) >> 16); 
                int green = ((grey & 0x0000FF00) >> 8); 
                int blue = (grey & 0x000000FF); 

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11); 
                if (grey > 128)
                {
                	grey = 255;
                	bufferFile[index/8] = (byte) (bufferFile[index/8] & ~(1 << (index%8)));
                }
                else
                {
                	grey = 0;
                	bufferFile[index/8] = (byte) (bufferFile[index/8] | (1 << (index%8)));
                }
                grey = alpha | (grey << 16) | (grey << 8) | grey; 
                pixels[index] = grey; 
            } 
        } 
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); 
        result.setPixels(pixels, 0, width, 0, 0, width, height); 
        return result; 
    } 
    
    private class SendTask extends AsyncTask<Void, Void, Void> 
	{
		// can use UI thread here
		protected void onPreExecute() 
		{						
			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
								
				int MemorySizeBytes = (Helper.ConvertStringToInt((dataDevice.getMemorySize().replace(" ", ""))) + 1)*4;
				
				//read binary file
				if (img_ready)
				{
					int fileSize = bufferFile.length;
					
					if ((fileSize % 4) > 0)
					{
						Toast toast = Toast.makeText(getApplicationContext(), "File format error : multiple of 4 bytes need (4 bytes per block)", Toast.LENGTH_SHORT);
			        	toast.show();
			        	//finish();
					}
					else if (fileSize > MemorySizeBytes)
					{
						Toast toast = Toast.makeText(getApplicationContext(), "File too big for your memory Tag", Toast.LENGTH_SHORT);
			        	toast.show();
			        	//finish();
					}
					else if (fileSize == 0 )
					{
						Toast toast = Toast.makeText(getApplicationContext(), "File empty", Toast.LENGTH_SHORT);
			        	toast.show();
			        	//finish();
					}
					else
					{
					    blocksToWrite = (int) fileSize / 4;					
					}	
				}
				else
				{					
					 Toast toast = Toast.makeText(getApplicationContext(), "Image is not ready.", Toast.LENGTH_SHORT);
		        	 toast.show();
		        	 //finish();
				}								
				
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			if (img_ready)
			{
				WriteSingleBlockAnswer = null;
				if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
		    	{						
		    		int ResultWriteAnswer = 0;
					for (int iAddressStart = 0; iAddressStart < blocksToWrite; iAddressStart++)
					{
						addressStart = Helper.ConvertIntTo2bytesHexaFormat(iAddressStart);
						dataToWrite[0] = bufferFile[iAddressStart*4];
						dataToWrite[1] = bufferFile[iAddressStart*4+1];
						dataToWrite[2] = bufferFile[iAddressStart*4+2];
						dataToWrite[3] = bufferFile[iAddressStart*4+3];
						cpt=0;					
						WriteSingleBlockAnswer = null;
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1) && cpt <= 10)
						{	
							WriteSingleBlockAnswer = NFCCommand.SendWriteSingleBlockCommand(dataDevice.getCurrentTag(), addressStart, dataToWrite, dataDevice);
							cpt++;
						}
						if(WriteSingleBlockAnswer[0]!=(byte)0x00)
						{
							ResultWriteAnswer = ResultWriteAnswer + 1;
							WriteSingleBlockAnswer[0] = (byte)0xE1;
							return null;
						}
						bar1.setProgress((int)(100*iAddressStart/blocksToWrite));
					}
					
					//now set the refresh enable flag
					addressStart = Helper.ConvertIntTo2bytesHexaFormat(2047);
					dataToWrite[0] = 0;
					dataToWrite[1] = 0;
					dataToWrite[2] = 0;
					dataToWrite[3] = 0;
					cpt=0;					
					WriteSingleBlockAnswer = null;
					while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1) && cpt <= 10)
					{	
						WriteSingleBlockAnswer = NFCCommand.SendWriteSingleBlockCommand(dataDevice.getCurrentTag(), addressStart, dataToWrite, dataDevice);
						cpt++;
					}
					if(WriteSingleBlockAnswer[0]!=(byte)0x00)
					{
						ResultWriteAnswer = ResultWriteAnswer + 1;
						WriteSingleBlockAnswer[0] = (byte)0xE1;
						return null;
					}					
					
					if(ResultWriteAnswer>0)
						WriteSingleBlockAnswer[0] = (byte)0xFF;
					else
						WriteSingleBlockAnswer[0] = (byte)0x00;
					
		    	}
			}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			if (img_ready) 
			{
				Boolean succ = false;
				if (WriteSingleBlockAnswer==null)				
				{
					Toast.makeText(getApplicationContext(), "ERROR Image Transfer (No tag answer) ", Toast.LENGTH_SHORT).show();
				}
				else if(WriteSingleBlockAnswer[0]==(byte)0x01)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR Image Transfer ", Toast.LENGTH_SHORT).show();
	    		}
	    		else if(WriteSingleBlockAnswer[0]==(byte)0xFF)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR Image Transfer ", Toast.LENGTH_SHORT).show();
	    		} 
	    		else if(WriteSingleBlockAnswer[0]==(byte)0xE1)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR Image Transfer process stopped", Toast.LENGTH_SHORT).show();
	    		}
	    		else if(WriteSingleBlockAnswer[0]==(byte)0x00)
	    		{
	    			Toast.makeText(getApplicationContext(), "Write Sucessfull ", Toast.LENGTH_SHORT).show();
	    			succ = true;
	    		}
	    		else
	    		{
	    			Toast.makeText(getApplicationContext(), "File Transfer ERROR ", Toast.LENGTH_SHORT).show();
	    		}
				
				if (!succ)
				{
					bar1.setProgress(0);
					bar1.setVisibility(ProgressBar.INVISIBLE);
				}
			}
    		
		}
	}

     //***********************************************************************/
  	 //* the function Decode the tag answer for the GetSystemInfo command
  	 //* the function fills the values (dsfid / afi / memory size / icRef /..) 
  	 //* in the myApplication class. return true if everything is ok.
  	 //***********************************************************************/
  	 public boolean DecodeGetSystemInfoResponse (byte[] GetSystemInfoResponse)
  	 {			 		 
  		 //if the tag has returned a god response 
  		 if(GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12)
  		 { 
  			 DataDevice ma = (DataDevice)getApplication();
  			 String uidToString = "";
  			 byte[] uid = new byte[8];
  			 // change uid format from byteArray to a String
  			 for (int i = 1; i <= 8; i++) 
  			 {
  				 uid[i - 1] = GetSystemInfoResponse[10 - i];
  				 uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
  			 }			 

  			 //***** TECHNO ******
  			 ma.setUid(uidToString);
  			 if(uid[0] == (byte) 0xE0)
  			 		 ma.setTechno("ISO 15693");
  			 else if (uid[0] == (byte) 0xD0)
  			 	 ma.setTechno("ISO 14443");
  			 else
  			 	 ma.setTechno("Unknown techno");			 
  			 			
  			 //***** MANUFACTURER ****
  			 if(uid[1]== (byte) 0x02)
  			 	 ma.setManufacturer("STMicroelectronics");
  			 else if(uid[1]== (byte) 0x04)
  			 	 ma.setManufacturer("NXP");
  			 else if(uid[1]== (byte) 0x07)
  			 	 ma.setManufacturer("Texas Instrument");
  			 else
  			 	 ma.setManufacturer("Unknown manufacturer");						 			
  			 			 
  			 //**** PRODUCT NAME *****
  			 if(uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07)
  			 {
  			 	 ma.setProductName("LRI512");
  			 	 ma.setMultipleReadSupported(false);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17)
  			 {
  			 	 ma.setProductName("LRI64");
  			 	 ma.setMultipleReadSupported(false);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23)
  			 {
  			 	 ma.setProductName("LRI2K");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B)
  			 {
  			 	 ma.setProductName("LRIS2K");
  			 	 ma.setMultipleReadSupported(false);	
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F)
  			 {
  			 	 ma.setProductName("M24LR64");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 }
  			 else if(uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43)
  			 {
  			 	 ma.setProductName("LRI1K");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47)
  			 {
  			 	 ma.setProductName("LRIS64K");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 }
  			 else if(uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B)
  			 {
  			 	 ma.setProductName("M24LR01E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F)
  			 {
  			 	 ma.setProductName("M24LR16E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  				 if(ma.isBasedOnTwoBytesAddress() == false)
  					return false;
  			 }
  			 else if(uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53)
  			 {
  			 	 ma.setProductName("M24LR02E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }
  			 else if(uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57)
  			 {
  			 	 ma.setProductName("M24LR32E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  				 if(ma.isBasedOnTwoBytesAddress() == false)
  				 	return false;			 	 
  			 }
  			 else if(uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B)
  			 {
  				 ma.setProductName("M24LR04E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 }
  			 else if(uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F)
  			 {
  			 	 ma.setProductName("M24LR64E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 	 if(ma.isBasedOnTwoBytesAddress() == false)
  			 		return false;
  			 }
  			 else if(uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63)
  			 {
  			 	 ma.setProductName("M24LR08E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 }
  			 else if(uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67)
  			 {
  			 	 ma.setProductName("M24LR128E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 	 if(ma.isBasedOnTwoBytesAddress() == false)
  				 	return false;			 	 
  			 }
  			 else if(uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F)
  			 {
  			 	 ma.setProductName("M24LR256E");
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  				 if(ma.isBasedOnTwoBytesAddress() == false)
  				 	return false;			 	 
  			 }
  			 else if(uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB)
  			 {
  			 	 ma.setProductName("detected product");
  			 	 ma.setBasedOnTwoBytesAddress(true);
  			 	 ma.setMultipleReadSupported(true);
  			 	 ma.setMemoryExceed2048bytesSize(true);
  			 }	 
  			 else
  			 {
  			 	 ma.setProductName("Unknown product");
  			 	 ma.setBasedOnTwoBytesAddress(false);
  			 	 ma.setMultipleReadSupported(false);
  			 	 ma.setMemoryExceed2048bytesSize(false);
  			 }		
  			 
  			 //*** DSFID ***
  			 ma.setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));
  			 
  			//*** AFI ***
  			 ma.setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));			 
  			 
  			//*** MEMORY SIZE ***
  			 if(ma.isBasedOnTwoBytesAddress())
  			 {
  				 String temp = new String();
  				 temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
  				 temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
  				 ma.setMemorySize(temp);
  			 }
  			 else 
  				 ma.setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));
  			 
  			//*** BLOCK SIZE ***
  			 if(ma.isBasedOnTwoBytesAddress())
  				 ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
  			 else
  				 ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

  			//*** IC REFERENCE ***
  			 if(ma.isBasedOnTwoBytesAddress())
  				 ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
  			 else
  				 ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
  				 
  			 return true;
  		 }
  		 
  		//if the tag has returned an error code 
  		 else
  			 return false;
  	 }
}
