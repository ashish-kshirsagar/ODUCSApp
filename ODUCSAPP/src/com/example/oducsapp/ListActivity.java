package com.example.oducsapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class ListActivity extends Activity {

	final static String XML_FILE_NAME = "oducsApp.xml";
	final static String TAG = "ListActivity";
	
	final String ROOT_NODE = "items";
	final String DIRECTORY_NODE = "directory";
	final String COURSE_NODE = "courses"; 
	final String EVENTS_NODE = "events";
	final String NEWS_NODE = "news";	
	final String CONTACT_NODE = "contact";
	final String DIR_CRSE_NODE = "course";
	
	final String DIR_CRSE_NAME = "name";
	final String DIR_CRSE_CRN = "CRN";		
	
	static Context cont;
	
	final static int ADD_CLICKD = 0;
	final static int DEL_CLICKD = 1;
	final static int MOD_CLICKD = 2;
	final static int SET_TIMER_CLICKD = 3;
	final static int RESET_TIMER_CLICKD = 4;
	final int MAX_COURSES_IN_DIR = 4;
	
	static int action = ADD_CLICKD;
	
	protected final static String FACULTY = "faculty";
	protected final static String STAFF = "Staff"; 
	
	final static String[] dir_content = {"type","name","position","office","phone","email","office_hour"};
	final static String[] courses_content = {"CRN","courseTitle","creditHours","Days","Time","classLocation","Instructor", "TA", "officeHours"};
	final static String[] events_content = {"type", "time", "day", "note"}; 
	final static String[] news_content = {"title", "keyword", "highlights", "pictures"};
	final static String[] contact_content = {"Name", "office", "email"};
	final CharSequence[] dir_types = {"faculty", "Staff"};
	final CharSequence[] events_add_types = {"Add record", "Add reminder"};
	final CharSequence[] action_types = {"Add Item", "Delete Item", "Mod Item"};
	final CharSequence[] action_types_events = {"Add Item", "Delete Item", "Mod Item", "Set Timer", "Reset Timer"};
	final CharSequence[] timer_action_types = {"Show current timer event", "Show timer event list"};
	
	static String selected_arr[] = new String[10];
	// these variables used to identify which item is being operated on
	static String SELECTED_NODE;	
	static AlertDialog userDialog;
		
	static String itemSelection = "";	
	ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
	
	//Timer service related variables.
	IntentFilter intentFilter;
	TimerService serviceBinder;
	Intent intnt;
	Ringtone ring;
	static int selectedIndex = -1;
	static int prevDayCount = -1;
	long sleepTime = 5000;	
	static int timerIndex[] = new int[20];	
	final int SEC_IN_A_DAY = 24 * 60 * 60;
	final int MILIS_IN_SEC = 1000;	
	final char days_arr[] = {' ', 'N', 'M', 'T', 'W', 'R', 'F', 'S'};	
	final String EVENT_TIMER_FILE = "EventTimerFile.txt";	
	private ShakeListener mShaker;
	WakeLock wl;
	
	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		cont = this;	
		
		//initialize timer index
		for(int i=0; i<20; i++)
			timerIndex[i] = -1;
			
		try
		{
			File f = new File(getCacheDir() + "/" + EVENT_TIMER_FILE);
			if(f.createNewFile())
			{				
				Log.d(TAG, "Event timer file created ---  " + f.getAbsolutePath());
			}
		}
		catch(Exception e)
		{ 
			Log.d(TAG, "Exception creating event timer file " + e.getMessage());
		}
		
		TextView listName = (TextView) findViewById(R.id.textView1);		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			itemSelection = extras.getString("selection_type");
		    
		    if(itemSelection.equals(MainActivity.ACTIVITY_DIRECTORY))
		    {
		    	SELECTED_NODE = DIRECTORY_NODE;
		    	selected_arr = dir_content;
		    	Log.d(TAG, "Selected list node " + SELECTED_NODE);
		    	listName.setText("Showing list for \n" + MainActivity.ACTIVITY_DIRECTORY.toUpperCase());
		    }
		    else if(itemSelection.equals(MainActivity.ACTIVITY_COURSES))
		    {
		    	SELECTED_NODE = COURSE_NODE;
		    	selected_arr = courses_content;
		    	Log.d(TAG, "Selected list node " + SELECTED_NODE);
		    	listName.setText("Showing list for \n" + MainActivity.ACTIVITY_COURSES.toUpperCase());
		    }
		    else if(itemSelection.equals(MainActivity.ACTIVITY_EVENTS))
		    {
		    	SELECTED_NODE = EVENTS_NODE;
		    	selected_arr = events_content;
		    	Log.d(TAG, "Selected list node " + SELECTED_NODE);
		    	listName.setText("Showing list for \n" + MainActivity.ACTIVITY_EVENTS.toUpperCase());
		    	
		    	//show menu button
		    	Button b1 = (Button)findViewById(R.id.menuBtn1);      		
        		b1.setVisibility(View.VISIBLE);
		    }
		    else if(itemSelection.equals(MainActivity.ACTIVITY_NEWS))
		    {
		    	SELECTED_NODE = NEWS_NODE;
		    	selected_arr = news_content;
		    	Log.d(TAG, "Selected list node " + SELECTED_NODE);
		    	listName.setText("Showing list for \n" + MainActivity.ACTIVITY_NEWS.toUpperCase());
		    }
		    else if(itemSelection.equals(MainActivity.ACTIVITY_CONTACT))
		    {
		    	SELECTED_NODE = CONTACT_NODE;
		    	selected_arr = contact_content;
		    	Log.d(TAG, "Selected list node " + SELECTED_NODE);
		    	listName.setText("Showing list for \n" + MainActivity.ACTIVITY_CONTACT.toUpperCase());
		    }
		}
		displayList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}	
	
	public void showUserDialog(final int position)
	{	
		AlertDialog ald1;
        //begin building alert dialog for asking type choice
		AlertDialog.Builder ald = new AlertDialog.Builder(cont);
		ald.setTitle("Action selector");
		
		if(itemSelection.equals(MainActivity.ACTIVITY_EVENTS))
		{	
			ald.setSingleChoiceItems(action_types_events, -1, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {				
					Log.d(TAG, "Selected action  " + which + "  " + action_types_events[which]);
					closeDialog();
					userAction(which, position);				
				}			
			});
		}
		else
		{
			ald.setSingleChoiceItems(action_types, -1, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {				
					Log.d(TAG, "Selected action  " + which + "  " + action_types[which]);
					closeDialog();
					userAction(which, position);				
				}			
			});
		} 
		//display dialog
		ald1 = ald.create();
		userDialog = ald1;		
		ald1.show();
	}
	
		
	public void displayList()
	{
		try
		{		
			//clear items if any before adding.
			menuItems.clear();
			
			int nodeCntr = 0;
			Document doc = getXMLObject();
			Log.d(TAG, "File opened successfully ...... ");			
			
			doc.getDocumentElement().normalize();	
	        NodeList nList = doc.getElementsByTagName(ROOT_NODE);        
	        if(nList == null)
	        {
	        	Log.d(TAG, "No root node " + ROOT_NODE + " found. Hence exiting.");
	        	return;
	        }
	        
	        Log.d(TAG, "Displaying list elements ...... ");
	        for (int temp = 0; temp < nList.getLength(); temp++) 
	        {
	            Node nNode = nList.item(temp);	            	            
	            if ((nNode != null)  &&  (nNode.getNodeType() == Node.ELEMENT_NODE)) 
	            {	
	            	if(nNode != null && nNode.hasChildNodes())
	            	{            		
	            		NodeList chldNodes =  nNode.getChildNodes();	            		
	            		for(int i=0; i<chldNodes.getLength(); i++)
	            		{	            			
	            			Node nn = chldNodes.item(i);
			            	if(nn != null && nn.getNodeName().equals(SELECTED_NODE))
			            	{
			            		nodeCntr++;			            		
			            		//hash map holds attribute values...
		            	        HashMap<String, String> map = new HashMap<String, String>();
		            	        
			            		//if this node has attributes then get all of them. 
			            		if(nn.hasAttributes())
			            		{
			            			NamedNodeMap attrs = nn.getAttributes();
			                        for (int index = 0; index < attrs.getLength(); index++) 
			                        {
			                            Attr attribute = (Attr) attrs.item(index);			                            
			                            map.put(attribute.getName(), attribute.getName().toUpperCase() + " : " + attribute.getValue());
			                        }
			                        
			                        //get child node values ... course
			                        if(SELECTED_NODE.equals(DIRECTORY_NODE))
			                        {			                        	
			                        	HashMap<String, String> map1 = getCourseNodeVal(doc, nodeCntr-1);
			                        	
			                        	if(map1 != null)
			                        	{
				                        	//parse child values and add....			            	        	
				                        	for (int index = 0; index < map1.size()/2; index++)
				                        	{
				                        		String cname = index + "cname";
					            	        	String crn = index + "cCRN";
				                        		map.put(crn, map1.get(crn));
				                        		map.put(cname, map1.get(cname));
				                        	}
			                        	}
			                        }
			            		}
			            		
				            	menuItems.add(map);
			            	}
	            		}
	            	}
	            }
	        }
	        
	        //Now show values in list view...
	        ListView list = (ListView)findViewById(R.id.list);
	        CustomAdapter adapter=new CustomAdapter(this, menuItems);
	        list.setAdapter(adapter);
	        list.setClickable(true);
	        
	        // Click event for single list row
	        list.setOnItemClickListener(new OnItemClickListener() {	 
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            	showUserDialog(position);
	            }
	        });
		}
		catch(Exception e)
		{
			Log.d(TAG, "Exception reading xml file........ " + e.getMessage());
		}
	}
	
	
	//get course node values of directory node.....
	public HashMap<String, String> getCourseNodeVal(Document docObj, int nodeCntr)
	{
		Log.d(TAG, "Displaying course details for a directory node");
		//hash map holds attribute values...
        HashMap<String, String> map = new HashMap<String, String>();
        NodeList nList = docObj.getElementsByTagName(DIRECTORY_NODE);      
        
        if(nList == null)
        {
        	Log.d(TAG, "No node found " + DIRECTORY_NODE + " hence exit.");
        	return map;
        }
        
        for (int temp = 0; temp < nList.getLength(); temp++) 
        {
        	if(temp != nodeCntr)
        		continue;
        	
            Node nNode = nList.item(temp);
            if(nNode != null && nNode.hasChildNodes())
	        {
	        	//nList.item(0).get;
	        	NodeList chldNodes =  nNode.getChildNodes();
	        	if(chldNodes != null)
	        	{
	        		int cnt = 0;
            		for(int i=0; i<chldNodes.getLength(); i++)
            		{
            			Node n1 = chldNodes.item(i);
            			
            			if(n1 != null && n1.getNodeType() == Node.ELEMENT_NODE)
            			{
            				if(n1.hasAttributes())
            				{
            					NamedNodeMap attrs = n1.getAttributes();
                        		for(int j=0; j<attrs.getLength(); j++)
                        		{	
                        			Attr attribute = (Attr) attrs.item(j);
                        			map.put(cnt + "c" + attribute.getNodeName(), attribute.getNodeValue());
                        		}
            				}
            				cnt++;
            			}
            		}
		        }
	        }
        }    
        
        Log.d(TAG, "Done displaying course details");
        
        return map;
	}
	
	
	public void userAction(int action, int selection)
	{
		switch(action)
		{
			case DEL_CLICKD:
				// Delete selected record
				Log.d(TAG, "Delete record selected... record No. " + selection);
				deleteRecord(selection);
				break;
			case MOD_CLICKD:
				// Modify selected record
				Log.d(TAG, "Edit record selected... record No. " + selection);
		        modifyRecord(selection);		        
				break;
			case ADD_CLICKD:
				// Add record selected	
				Log.d(TAG, "Add record selected... record clicked No. " + selection);
		        addRecord(selection);
				break;
			case SET_TIMER_CLICKD:
			case RESET_TIMER_CLICKD:
				// Add record selected	
				Log.d(TAG, "Set timer selected... record clicked No. " + selection);
		        setResetTimer(action, selection, "-2", "", "");
				break;
			default:
				// do nothing
				break;			
		}
	}	
	
	
	public void addRecord(final int position)
	{    
        Log.d(TAG, "Selected add record"); 
        
        //if directory node selected then ask for type of record.
        if(SELECTED_NODE.equals(DIRECTORY_NODE))
        {
	        AlertDialog ald1;
	        //begin building alert dialog for asking type choice
			AlertDialog.Builder ald = new AlertDialog.Builder(cont);
			ald.setTitle("Type selector");
			ald.setSingleChoiceItems(dir_types, -1, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {				
					Log.d(TAG, "Selected choice  " + which + "  " + dir_types[which]);
					closeDialog();
					addRecordDialog(which, position);				
				}			
			});
			 
			//display dialog
			ald1 = ald.create();
			userDialog = ald1;		
			ald1.show();
        }
        else
        {
        	addRecordDialog(0, position);
        }
	}
	
	
	//------------------------------------------------------------------------------
	//Timer service related code
	//------------------------------------------------------------------------------
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			//—-called when the connection is made—-
			serviceBinder = ((TimerService.MyBinder)service).getService();
			
			serviceBinder.sleepTime = sleepTime;
			serviceBinder.timerForEventIndex = selectedIndex;
			
			startService(intnt);			
			Log.d(TAG, "Service started ");
		}
		
		public void onServiceDisconnected(ComponentName className) {
			//---called when the service disconnects---
			serviceBinder = null;
		}
	};
	
	public void startService() {
		intnt = new Intent(ListActivity.this, TimerService.class);
		bindService(intnt, connection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//---intent to filter for timer service intent---
		intentFilter = new IntentFilter();
		intentFilter.addAction("TIMER_GOES_OFF");
		//---register the receiver---
		registerReceiver(intentReceiver, intentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		//---unregister the receiver---
		unregisterReceiver(intentReceiver);
	}
	
	public void stopTimerService() {
		int cnt = serviceBinder.getCount();
		Log.d(TAG, "Stop service called ");
		
		stopService(new Intent(getBaseContext(), TimerService.class));
		Toast.makeText(getBaseContext(), "Service stopped!", Toast.LENGTH_LONG).show();	
		
		stopAlarm();
	}
	
	public void stopAlarm()
	{
		if(ring != null && ring.isPlaying())
		{
			ring.stop();
			selectedIndex = -1;
			sleepTime = MILIS_IN_SEC;
			if(userDialog != null && userDialog.isShowing())
				userDialog.dismiss();
			
			//If wake lock is held then release it.
			if(wl != null && wl.isHeld())
			{
				wl.release();
				wl = null;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void startAlarm()
	{
		stopTimerService();
		
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	    if(alert == null){
	    	Log.d(TAG, "Set alarm for type notification");
	         // alert is null, using backup
	         alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	         if(alert == null){  
	             // alert backup is null, using 2nd backup
	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
	         }
	    }
	    else
	    {
	    	Log.d(TAG, "Set alarm for type_alarm");
	    }
	    
	    //Get the wake lock so as to keep device running till timer
	    //expire
	    PowerManager pm = (PowerManager)cont.getSystemService(Context.POWER_SERVICE);
	    wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
	    
	    ring = RingtoneManager.getRingtone(getApplicationContext(), alert);
	    
	    //Register for vibrator broadcast
	    final Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	    mShaker = new ShakeListener(this);
	    mShaker.setOnShakeListener(new ShakeListener.OnShakeListener () {
		    public void onShake()
		    {
		        vibe.vibrate(300);
		        stopAlarm();		        
		    }
	    });
	    
	    
	    //Acquire this lock only when required.
	    wl.acquire();	    
	    ring.play();
	}
	
		
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			int cnt = serviceBinder.getCount();
			Log.d(TAG, "Broadcast received. Count " + cnt);
			
			startAlarm();			
			showEventTimerList(true);
		}
	};
	
	//-----------------------------------------------------------------------------------------------------
	
	
	public void showEventStatus(View view)
	{
		AlertDialog ald1;
        //begin building alert dialog for asking type choice
		AlertDialog.Builder ald = new AlertDialog.Builder(cont);
		ald.setTitle("Select option");
		ald.setSingleChoiceItems(timer_action_types, -1, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				Log.d(TAG, "Selected timer choice  " + which + "  " + timer_action_types[which]);
				closeDialog();
				if(which == 0)
				{
					showEventTimerList(true);
				}
				else
				{
					showEventTimerList(false);
				}
			}			
		});
		 
		//display dialog
		ald1 = ald.create();
		userDialog = ald1;		
		ald1.show();
	}
	
		
	public void showEventTimerList(boolean onlyLatest)
	{
		if(selectedIndex == -1 && onlyLatest)
		{
			Toast.makeText(getBaseContext(), "No current timer set!", Toast.LENGTH_LONG).show();
			return;
		}
		
		ArrayList<String> lines = new ArrayList<String>();
		
		//read event details from the file
	    try
	    {
	    	File f = new File(getCacheDir() + "/" + EVENT_TIMER_FILE);
	    	if(f.exists() && f.canRead())
	    	{	    		
	    	    FileInputStream fstream = new FileInputStream(f);
	    	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	    	    String strLine;
	    	    while ((strLine = br.readLine()) != null) {
	    	        lines.add(strLine);
	    	        Log.d(TAG, "Timer from file :  " + strLine);
	    	    }
	    	    fstream.close();
	    	}
	    	else
	    	{
	    		Log.d(TAG, "Timer event file not present ....");
		    	Toast.makeText(getBaseContext(), "No stored timer events found !!", Toast.LENGTH_LONG).show();
		    	return;
	    	}
	    }
	    catch(Exception e)
	    {
	    	Log.d(TAG, "Exception opening event file....");
	    	Toast.makeText(getBaseContext(), "No stored timer events found !!", Toast.LENGTH_LONG).show();
	    	return;
	    }       
        
        //show dialog box with old values and ask for modifications.
        //begin building alert dialog 
		AlertDialog.Builder al = new AlertDialog.Builder(cont);
		if(onlyLatest)
			al.setTitle("Current timer event");
		else
			al.setTitle("Timer Events List");
		
		al.setIcon(R.drawable.directory_icon);		
		
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cont, android.R.layout.select_dialog_item);
		
		for(int i=lines.size()-1; i >=0; i--)
		{	
			String str = "";
			String temp[] = lines.get(i).split(",");			
			
			for(int j=0; j<temp.length; j++)
			{
				String temp1[] = temp[j].split("=");
				temp1[1] = temp1[1].replace("}", "");
				str = str + "" + temp1[1] + "\n";
			}		
			
			Log.d(TAG, "Adding to array : " + str);
	        arrayAdapter.add(str);
	        
	        //break after showing last record from file. as it is current record.
	        if(onlyLatest)
	        	break;
		}
		
		al.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() {
            	@Override
            	public void onClick(DialogInterface dialog, int which) {
            		dialog.dismiss();
            	}
        	});
		
		al.setAdapter(arrayAdapter,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	//nothing
                }
            });
		
		AlertDialog ald1;
        ald1 = al.create();
        userDialog = ald1;
        ald1.show();
	}
	
	public void takeTimeValue(final int action, final int pos)
	{
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);    

	    LinearLayout lila1= new LinearLayout(this);
	    lila1.setOrientation(1); //1 is for vertical orientation
	    final EditText hours = new EditText(this);
	    hours.setHint("Hours(HH)");
	    final EditText minutes = new EditText(this);
	    minutes.setHint("Minutes(MM)");
	    final EditText seconds = new EditText(this);
	    seconds.setHint("Seconds(SS)");
	    lila1.addView(hours);
	    lila1.addView(minutes);
	    lila1.addView(seconds);
	    alert.setView(lila1);

        alert.setIcon(R.drawable.directory_icon);
        alert.setTitle("Select reminder time");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {             
            public void onClick(DialogInterface dialog, int whichButton) {              
                	String min = minutes.getText().toString().trim();                       
                	String sec = seconds.getText().toString().trim();  
                	String hrs = hours.getText().toString().trim();
                	Log.d(TAG, "Entered values =" + hrs + ":" + min + ":" + sec);
                	
                	setResetTimer(action, pos, sec, min, hrs);                	
                }
            });                 
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {                           
            public void onClick(DialogInterface dialog, int whichButton) {          
                dialog.cancel();    
            }
        });
        alert.show();
	}
	
	public void setResetTimer(final int action, final int pos, String sec, String min, String hrs)
	{	
		switch(action)
		{
			case SET_TIMER_CLICKD:
				sleepTime = 10000;
			
				if(sec.equals("-2"))
				{
					Log.d(TAG, "Asking for timer values... " + pos);
					takeTimeValue(action, pos);
					return;
				}
				
				Log.d(TAG, "Continueing to start timer ... " + pos);
				
				//if there is old timer already set then just show message
				if(selectedIndex == pos)
				{
					Toast.makeText(getBaseContext(), "Timer already set!!", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Old timer already set for item index " + pos);
					return;
				}
				
				if (hrs.equals(""))
					hrs = "0";
				if (min.equals(""))
					min = "0";
				if (sec.equals(""))
					sec = "0";
				
				int h = 0, m = 0, s = 0;
				try
				{
					h = Integer.parseInt(hrs);
					m = Integer.parseInt(min);
					s = Integer.parseInt(sec);
					
					if(m > 60 || s > 60)
						throw new Exception();
				}
				catch(Exception e)
				{
					Toast.makeText(getBaseContext(), "Invalid time!!", Toast.LENGTH_SHORT).show();
					return;
				}				
							
				//get records from selected item
				HashMap<String, String> item = new HashMap<String, String>();
		        item = menuItems.get(pos);        
		        Log.d(TAG, "Selected modify record for item = " + item.toString());
		        
		        String ev_arr[] = new String[10];
		        String dayStr1 = "";
				int i=0;
				int hh = 0, mm = 0;
				for(i=0; i<selected_arr.length; i++)
				{	
					String s1 = item.get(selected_arr[i]).replaceFirst(":", "#");
					String temp[] = s1.split("#");					
					ev_arr[i] = temp[1].trim();
					String ss = temp[0].trim().toLowerCase();
					if(ss.contains("day"))
					{
						dayStr1 = ev_arr[i].trim();
					}
					ss = temp[0].trim().toLowerCase();
					if(ss.contains("time"))
					{
						ss = temp[1].trim();
						String ampm = ss.substring(ss.length()-2, ss.length());
						String temp1[] = ss.split("-");
						String startTime = temp1[0].trim();
						String temp2[] = startTime.split(":");
						
						try
						{
							hh = Integer.parseInt(temp2[0]);
							mm = Integer.parseInt(temp2[1]);
						}
						catch(Exception e)
						{
							Log.d(TAG, "Exception - converting time");
						}
						
						//convert to 24 hrs clock
						if(ampm.equalsIgnoreCase("pm"))
						{
							hh = hh + 12;
						}											
					}
				}				
				
				Calendar calendar = Calendar.getInstance();
				int day = calendar.get(Calendar.DAY_OF_WEEK);				

				int dayCnt = 0;
				String dayStr = "";
				switch (day) {
			    	case Calendar.SUNDAY:
			        	dayStr = "S";
			    		break;	
			    	case Calendar.MONDAY:
			    		dayStr = "M";
			    		break;
			    	case Calendar.TUESDAY:
			        	dayStr = "T";
			    		break;
			    	case Calendar.WEDNESDAY:
			        	dayStr = "W";
			    		break;
			    	case Calendar.THURSDAY:
			        	dayStr = "R";
			    		break;
			    	case Calendar.FRIDAY:
			        	dayStr = "F";
			    		break;
			    	case Calendar.SATURDAY:
			        	dayStr = "S";
			    		break;			    	
				}
				
				//find day index for event
				char dy = dayStr1.charAt(0);
				for(int j=1; j<8; j++)
				{
					if(dy == days_arr[j])
					{
						dayCnt = j;
						break;
					}
				}
				
				int diff = 0;
				if(dayCnt > day)
					diff = dayCnt - day;
				else
					diff = day - dayCnt;
				
				boolean flag = true;
				
				// if same day then different timer.
				if(diff == 0)
				{
					// find difference between current time and event time
					int time1 = ((hh * 60 * 60) + (mm * 60)) - ((h * 60 * 60) + (m * 60) + (s));
					Calendar c = Calendar.getInstance();
					int time2 = (c.get(Calendar.HOUR_OF_DAY) * 60 * 60) + (c.get(Calendar.MINUTE) * 60);
					diff = time1 - time2;
					sleepTime = diff;
					if(diff <= 0)
					{
						flag = true;
					}
					else
					{
						flag = false;
					}
				}
				
				if(flag == true)
				{					
					Calendar c = GregorianCalendar.getInstance();
					int timeRemain = SEC_IN_A_DAY - ((c.get(Calendar.HOUR_OF_DAY) * 60 * 60) + (c.get(Calendar.MINUTE) * 60));
					int timeLapsed = (hh * 60 * 60) + (mm * 60); 
					
					// time for timer delay is calculated as below
					// difference of complete days in these two days +
					// time remaining from day before and time lapsed on second day
					int timeForTimer = (diff * SEC_IN_A_DAY) + timeRemain + timeLapsed;
					int timerBeforeEvent = (h * 60 * 60) + (m * 60) + (s);
					
					// hence sleep time is total time remaining to the even - remainder before time
					sleepTime = timeForTimer - timerBeforeEvent;
				}
				
				sleepTime = sleepTime * MILIS_IN_SEC;
				Log.d(TAG, "Starting timer for time duration --- " + sleepTime);
				
				prevDayCount = day;
				selectedIndex = pos;				
			    
			    //write event details to the file
			    try
			    {
			    	File f = new File(getCacheDir() + "/" + EVENT_TIMER_FILE);
			    	if(f.exists() && f.canWrite())
			    	{
			    		FileWriter writer = new FileWriter(f, true);
			    		writer.append(item.toString() +"\n");
			    		writer.close();
			    		Log.d(TAG, "Writing timer record to file .... " + item.toString());
			    	}
			    }
			    catch(Exception e)
			    {
			    	Log.d(TAG, "Exception opening event file....");
			    }
			    			    
				startService();
							
				Log.d(TAG, "Alarm set successfully ");
				
				break;
			case RESET_TIMER_CLICKD:
				//if there is no old timer already set then just show message
				if(selectedIndex != pos)
				{
					Toast.makeText(getBaseContext(), "Timer not set!!", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Timer not set for item index " + pos);					
					return;
				}
				
				stopTimerService();
				
				selectedIndex = -1;
				
				break;
		}
	}
	
	
	public void addRecordDialog(final int choice, final int pos)
	{	
		//show dialog box with old values and ask for modifications.
        //begin building alert dialog 
		AlertDialog.Builder al = new AlertDialog.Builder(cont);
		al.setTitle("Add Record");
		al.setMessage("Enter values to add record");			    
		al.setIcon(R.drawable.directory_icon);		
		    
		// Create layout dynamically and add to dialog		
		final LinearLayout layout = new LinearLayout(cont);
		layout.setOrientation(LinearLayout.VERTICAL);		
		layout.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);		
		ScrollView sv = new ScrollView(cont);	
		
		// Now add edit boxes in the dialog box
		final TextView[] tv = new TextView[20];
		final EditText[] text = new EditText[20];
		int i=0;		
		for(i=0; i<selected_arr.length; i++)
		{	
			//add title for edit box
			tv[i] = new TextView(cont);
			tv[i].setText(selected_arr[i].toUpperCase());
    		tv[i].setTextColor(Color.RED);    		
    		    		
    		//add edit box
    		text[i] = new EditText(cont);
    		//text[i].setText("sample " + i);
    		
    		//for directory node set first record type non editable...
    		if(SELECTED_NODE.equals(DIRECTORY_NODE) && i == 0)
    		{
    			text[i].setText(dir_types[choice]);
    			text[i].setEnabled(false);
    		}
    		
    		layout.addView(tv[i]);
    		layout.addView(text[i]);
		}
		
		final int editTextCnt1 = i;
		
		// if selected node is directory and type is faculty then add boxes for courses 
		if(SELECTED_NODE.equals(DIRECTORY_NODE) && dir_types[choice].toString().equalsIgnoreCase(FACULTY))
		{
			int j = 0;
	    	while(j < MAX_COURSES_IN_DIR)
	    	{	
	    		//set CRN 
	    		tv[i] = new TextView(cont);
	    		tv[i].setText(("CRN " + (j+1)).toUpperCase());
	    		tv[i].setTextColor(Color.RED);    		
	    		layout.addView(tv[i]);
	    		
	    		text[i] = new EditText(cont);
	    		layout.addView(text[i]);
	    		
	    		i++;
	    		
	    		//set course name
	    		tv[i] = new TextView(cont);
	    		tv[i].setText(("Course Name " + (j+1)).toUpperCase());
	    		tv[i].setTextColor(Color.RED);    		
	    		layout.addView(tv[i]);
	    		
	    		text[i] = new EditText(cont);
	    		layout.addView(text[i]);
	    		
	    		i++;
	    		j++;  		
	    	}
		}
		
    	final int editTextCnt2 = i;			
		
		//add button in the layout
		final Button btnAdd = new Button(cont);
		btnAdd.setText("Add Record");
		btnAdd.setBackgroundResource(R.drawable.button_shape);
		btnAdd.setHeight(60);
		btnAdd.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				try
				{
					int i=0;
					if(SELECTED_NODE.equals(DIRECTORY_NODE))
					{						
						//check if both CRN and course name pair entered
						for(i=editTextCnt1 ;i<editTextCnt2; i++)
						{
							if((text[i].getText().equals("") && !text[i+1].getText().equals("")) ||
							   (!text[i].getText().equals("") && text[i+1].getText().equals("")))
							{
								Toast.makeText(cont, "CRN and Course name both should be present", Toast.LENGTH_SHORT).show();
								return;
							}					
							i++;					
						}
					}
					
					int emptycntr = 0;
					//check if at least one value entered
					for(i=0 ;i<editTextCnt2; i++)
					{
						if(text[i].getText().toString().trim().equals(""))
						{
							emptycntr++;
						}
					}

					if(emptycntr == editTextCnt2)
					{
						Toast.makeText(cont, "Cannot add empty information !!!", Toast.LENGTH_SHORT).show();
						return;
					}
					
					Document dom = getXMLObject();
					//Element root = dom.getDocumentElement();
					NodeList rootList = dom.getElementsByTagName(ROOT_NODE);
					Node root = rootList.item(0);
					
					//dom.appendChild(root);
					
					Element newelem = dom.createElement(SELECTED_NODE);
					for(i=0; i<selected_arr.length; i++)
					{
						String val = text[i].getText().toString();
						String key = selected_arr[i];
						newelem.setAttribute(key, val);
					}
					
					//if it is directory node then see if any courses need to be added.
					if(SELECTED_NODE.equals(DIRECTORY_NODE))
					{
						//Now add course list if any						
						for(;i<editTextCnt2; i++)
						{
							//if CRN field not empty then add it to list
							if((text[i].getText().length() > 0) && (text[i].getText().length() > 0))
							{
								Element chld = dom.createElement(DIR_CRSE_NODE);
								chld.setAttribute(DIR_CRSE_CRN, text[i].getText().toString());
								chld.setAttribute(DIR_CRSE_NAME, text[i+1].getText().toString());
								newelem.appendChild(chld);
							}
							i++;						
						}
					}
										
					root.appendChild(newelem);
					
					//close user dialog
					closeDialog();
					
					//update new items to the file.
					if(WriteFile(dom))
						Toast.makeText(cont, "Record added successfully !! ", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(cont, "Error added record, try again!! ", Toast.LENGTH_SHORT).show();
					
					//display new list again.
					displayList();					
				}
				catch(Exception e)
				{
					Log.d(TAG, "Exception reading file for modification " + e.getMessage());
					Toast.makeText(cont, "Error adding record, try again!! ", Toast.LENGTH_SHORT).show();
				}
			}
		});
		layout.addView(btnAdd);
		
		//set layout for dialog
    	sv.addView(layout);
		al.setView(sv);
    	
		// Create alert dialog
	    final AlertDialog al1 = al.create();
	    userDialog = al1;
	    
		//display dialog box
		al1.show();
	}
	
	public void deleteRecord(final int position)
	{
		Document doc = getXMLObject();
		Log.d(TAG, "File opened successfully ...... deleting record no   " + position);			
		
		doc.getDocumentElement().normalize();
		Element element = (Element) doc.getElementsByTagName(SELECTED_NODE).item(position);
		element.getParentNode().removeChild(element);
		
		doc.normalize();
		
		//write updated record to file...
		if(WriteFile(doc))
			Toast.makeText(cont, "Record deleted successfully !! ", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(cont, "Error deleting record, try again!! ", Toast.LENGTH_SHORT).show();	
		
		Log.d(TAG, "Record removed successfully....");
		
		//display new list again.
		displayList();
	}
	
		
	public void modifyRecord(final int position)
	{		
		HashMap<String, String> item = new HashMap<String, String>();
        item = menuItems.get(position);        
        Log.d(TAG, "Selected modify record for item = " + item.toString());        
        
        //show dialog box with old values and ask for modifications.
        //begin building alert dialog 
		AlertDialog.Builder al = new AlertDialog.Builder(cont);
		al.setTitle("Records Modifier");
		al.setMessage("Modify values");			    
		al.setIcon(R.drawable.directory_icon);		
		    
		// Create layout dynamically and add to dialog		
		final LinearLayout layout = new LinearLayout(cont);
		layout.setOrientation(LinearLayout.VERTICAL);		
		layout.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);		
		ScrollView sv = new ScrollView(cont);	
		
		// Now add edit boxes in the dialog box
		final TextView[] tv = new TextView[20];
		final EditText[] text = new EditText[20];		
		int i=0;
		for(i=0; i<selected_arr.length; i++)
		{	
			String ss = item.get(selected_arr[i]);
			ss = ss.replaceFirst(":", "#");
			String temp[] = ss.split("#");			
			
			//add title for edit box
			tv[i] = new TextView(cont);
			tv[i].setText(temp[0].trim().toUpperCase());
    		tv[i].setTextColor(Color.RED);    		
    		layout.addView(tv[i]);
    		
    		//add edit box with existing text
    		text[i] = new EditText(cont);			
			text[i].setText(temp[1].trim());
			layout.addView(text[i]);
		}
		
		final int editTextCnt1 = i;
		
		//if selected is directory node then check if there are any course nodes available.
		if(SELECTED_NODE.equals(DIRECTORY_NODE))
		{
			// if there are courses added in directory then show them 
			int j = 0;
	    	String cname = j + "cname";
	    	String crn = j + "cCRN";
	    	while(item.containsKey(cname))
	    	{	
	    		tv[i] = new TextView(cont);
	    		tv[i].setText(("Course " + (j+1)).toUpperCase());
	    		tv[i].setTextColor(Color.RED);    		
	    		layout.addView(tv[i]);
	    		
	    		//set CRN 
	    		text[i] = new EditText(cont);
	    		text[i].setText(item.get(crn));
	    		layout.addView(text[i]);
	    		
	    		//set course name
	    		i++;
	    		text[i] = new EditText(cont);
	    		text[i].setText(item.get(cname));
	    		layout.addView(text[i]);
	    		
	    		i++;
	    		j++;
	    		cname = j + "cname";
	    		crn = j + "cCRN";    		
	    	}
		}
    	final int editTextCnt2 = i;
		
		//add modify button in the layout
		final Button btnMod = new Button(cont);
		btnMod.setText("Modify Record");
		btnMod.setBackgroundResource(R.drawable.button_shape);
		btnMod.setHeight(60);
		btnMod.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				try
				{
					//Modify only if faculty or staff record type
					if(SELECTED_NODE.equals(DIRECTORY_NODE) && 
					   !(text[0].getText().toString().equalsIgnoreCase(FACULTY) ||
								text[0].getText().toString().equalsIgnoreCase(STAFF)))
					{
						Log.d(TAG, "Invalid record type");
						Toast.makeText(cont, "Record type should be either " + STAFF + " or " + FACULTY, Toast.LENGTH_SHORT).show();
						return;
					}
					
					Document doc = getXMLObject();
					Log.d(TAG, "File opened successfully ...... modifying record no " + position);			
					
					doc.getDocumentElement().normalize();
					Element element = (Element) doc.getElementsByTagName(SELECTED_NODE).item(position);
					element.getParentNode().removeChild(element);
					NodeList rootList = doc.getElementsByTagName(ROOT_NODE);
					Node root = rootList.item(0);
					
					
					int i=0;
					Element newelem = doc.createElement(SELECTED_NODE);
					for(i=0; i<selected_arr.length; i++)
					{
						String val = text[i].getText().toString();
						String key = selected_arr[i];
						newelem.setAttribute(key, val);
					}
					
					//if it is directory node then see if any courses need to be added.
					if(SELECTED_NODE.equals(DIRECTORY_NODE))
					{
						//Now add course list if any
						Log.d(TAG, "Course count  " + editTextCnt2 + " - " + editTextCnt1 + " " + (editTextCnt2-editTextCnt1));
						for(;i<editTextCnt2; i++)
						{
							//if CRN field not empty then add it to list
							if((text[i].getText().length() > 0) && (text[i].getText().length() > 0))
							{
								Log.d(TAG, "Adding course line to list --- " + text[i].getText() + " :  " + text[i+1].getText());
								
								Element chld = doc.createElement(DIR_CRSE_NODE);
								chld.setAttribute(DIR_CRSE_CRN, text[i].getText().toString());
								chld.setAttribute(DIR_CRSE_NAME, text[i+1].getText().toString());
								newelem.appendChild(chld);
							}
							i++;						
						}
					}					
					
					root.appendChild(newelem);
				
					//close user dialog
					closeDialog();
					
					//update new items to the file.
					if(WriteFile(doc))
						Toast.makeText(cont, "Record modified successfully !! ", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(cont, "Error modifying record, try again!! ", Toast.LENGTH_SHORT).show();
					
					//display new list again.
					displayList();
					
				}
				catch(Exception e)
				{
					Log.d(TAG, "Exception reding file for modification " + e.getMessage());
					Toast.makeText(cont, "Error modifing record, try again!! ", Toast.LENGTH_SHORT).show();
				}
			}
		});
		layout.addView(btnMod);
		
		//set layout for dialog
    	sv.addView(layout);
		al.setView(sv);
    	
		// Create alert dialog
	    final AlertDialog al1 = al.create();
	    userDialog = al1;
	    
		//display dialog box
		al1.show();
	}
	
	
	public void closeDialog()
	{
		userDialog.cancel();
	}
	
	
	public boolean WriteFile(Document dom)
	{			
		boolean status = false;
		
		try
		{
			File f = getFilePath();			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(dom);
			StreamResult streamResult = new StreamResult(f);
			transformer.transform(domSource, streamResult);		
			status = true;
		}
		catch(Exception e)
		{
			Log.d(TAG, "Exception writing to XML file....");
		}
		
					
		return status;
	}
	
		
	public File getFilePath()
	{
		File f = new File(MainActivity.FilePath);
		return f;
	}
	
	public Document getXMLObject()
	{
		Document doc = null;
		
		try
		{
			File f = new File(MainActivity.FilePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(f);
		}
		catch(Exception e)
		{
			Log.d(TAG, "Exception in opening XML file  " + e.getMessage());
		}
		return doc;
	}
}


