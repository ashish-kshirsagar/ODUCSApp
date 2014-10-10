package com.example.oducsapp;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	final static String TAG1 = "ODUCSAPP";
	
	static boolean mExternalStorageAvailable = false;
	static boolean mExternalStorageWriteable = false;
	
	final static String XML_FILE_NAME = "oducsapp.xml";
	
	final static String ACTIVITY_DIRECTORY = "directory";
	final static String ACTIVITY_COURSES = "courses";
	final static String ACTIVITY_EVENTS = "events";
	final static String ACTIVITY_NEWS = "news";
	final static String ACTIVITY_CONTACT = "contact";
	final static String ACTIVITY_ABOUT = "about";
	
	final static String ROOT_NODE = "items";
	
	static Activity act = null;
	
	public static String FilePath = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void performAction(View v)
	{
		Button btn = null;
		Intent list = new Intent(this, ListActivity.class);
		boolean filePresent = true;
		
		Log.d(TAG1, "Check if file present...");
				
		if(!checkFileProper(this))
		{	
			filePresent = false;
		}
		
		switch(v.getId())
		{
			case R.id.Button1:
				if(!filePresent)
				{
					Toast.makeText(this, "XML file not found or incorrect format!!!", Toast.LENGTH_SHORT).show();
					break;
				}
				btn = (Button)findViewById(R.id.Button1);
				Log.d(TAG1, btn.getText() + " Clicked !!");
				list.putExtra("selection_type", ACTIVITY_DIRECTORY);
				startActivity(list);
				break;
			case R.id.Button01:
				if(!filePresent)
				{
					Toast.makeText(this, "XML file not found or incorrect format!!!", Toast.LENGTH_SHORT).show();
					break;
				}
				btn = (Button)findViewById(R.id.Button01);
				Log.d(TAG1, btn.getText() + " Clicked !!");
				list.putExtra("selection_type",ACTIVITY_COURSES);
				startActivity(list);
				break;
			case R.id.Button02:
				if(!filePresent)
				{
					Toast.makeText(this, "XML file not found or incorrect format!!!", Toast.LENGTH_SHORT).show();
					break;
				}
				btn = (Button)findViewById(R.id.Button02);
				Log.d(TAG1, btn.getText() + " Clicked !!");
				list.putExtra("selection_type",ACTIVITY_EVENTS);
				startActivity(list);
				break;
			case R.id.Button03:
				if(!filePresent)
				{
					Toast.makeText(this, "XML file not found or incorrect format!!!", Toast.LENGTH_SHORT).show();
					break;
				}
				btn = (Button)findViewById(R.id.Button03);
				Log.d(TAG1, btn.getText() + " Clicked !!");
				list.putExtra("selection_type",ACTIVITY_NEWS);
				startActivity(list);
				break;
			case R.id.Button04:
				if(!filePresent)
				{
					Toast.makeText(this, "XML file not found or incorrect format!!!", Toast.LENGTH_SHORT).show();
					break;
				}
				btn = (Button)findViewById(R.id.Button04);
				Log.d(TAG1, btn.getText() + " Clicked !!");
				list.putExtra("selection_type",ACTIVITY_CONTACT);
				startActivity(list);
				break;
			case R.id.Button05:
				btn = (Button)findViewById(R.id.Button05);
				Log.d(TAG1, btn.getText() + " Clicked !!");
				list.putExtra("selection_type",ACTIVITY_ABOUT);
				if(FilePath != null)
				{
					Intent settings = new Intent(this, AboutSettings.class);
					startActivity(settings);
				}
				break;
		}
	}
	
	public static void checkExternalStorage()
	{
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state))
		{
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;		    
		    Log.d(TAG1, "External storage found and writable");
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		    Log.d(TAG1, "External storage found but not writable");
		}
		else 
		{
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		    Log.d(TAG1, "External storage not found");
		}
	}
	
	public static boolean checkXMLFile(Context cont)
	{
		
		// if external storage is proper then check if file exists.
		if(mExternalStorageAvailable && mExternalStorageWriteable)
		{
			File extPath = Environment.getExternalStorageDirectory();
			File filePath = new File(extPath, XML_FILE_NAME);
			
			if(filePath.exists())
				return true;
			else
				return false;
		}
		// external storage unavailable or not writable hence return false
		else
		{
			return false;
		}
	}
	
	
	public boolean checkFileProper(Context cont)
	{			
		boolean status = false;
		
		if(FilePath == null)
		{
			Toast.makeText(this, "Update the file path before proceeding", Toast.LENGTH_SHORT).show();
			Intent settings = new Intent(this, AboutSettings.class);
			startActivity(settings);
		}
		
		try
		{		
			File f = new File(MainActivity.FilePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);
			
			doc.getDocumentElement().normalize();	
	        NodeList nList = doc.getElementsByTagName("items");	        
	        Log.d(TAG1, "Nodelist length  " + nList.getLength());
	        
	        if(nList.getLength() > 0)
	        	status = true;
	        else
	        	status = false;
		}
		catch(Exception e)
		{
			Log.d(TAG1, "Exception reading xml file........ " + e.getMessage());
		}
		
		// return status 
		return status;
	}
}
