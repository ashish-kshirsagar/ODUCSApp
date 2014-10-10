package com.example.oducsapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
 
@SuppressLint("DefaultLocale")
public class CustomAdapter extends BaseAdapter {
 
    private static Activity activity;
    private static ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
 
    public CustomAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;        
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint("DefaultLocale")
	public View getView(int position, View convertView, ViewGroup parent) {
    	int crsetv[] = {R.id.TextView12, R.id.TextView07, R.id.TextView08, R.id.TextView09, R.id.TextView10, R.id.TextView11};
    	int tvlist[] = {R.id.TextView05, R.id.TextView04, R.id.TextView03, R.id.TextView02, R.id.TextView01, R.id.TextView1, R.id.TextView06, R.id.TextView13, R.id.TextView14}; 
    	
        View vi=convertView;
        vi = inflater.inflate(R.layout.directory_layout, null);
         
        TextView tv[] = new TextView[12];
        
        HashMap<String, String> item = new HashMap<String, String>();        
        item = data.get(position);        
        
        if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_DIRECTORY))
	    {
        	int i = 0;
        	for(i=0; i<ListActivity.dir_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.dir_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
        	
        	//Add courses the view if exists and only for faculty type
            if(item.get("type").toUpperCase().contains(ListActivity.FACULTY.toUpperCase()))
            {
    	        if(item.containsKey("0cname"))
    	        {	
    	        	TextView tvc = (TextView)vi.findViewById(crsetv[0]);
    	        	tvc.setVisibility(View.VISIBLE);
    	        	i = 0;
    	        	String cname = i + "cname";
    	        	String crn = i + "cCRN";
    	        	while(item.containsKey(cname))
    	        	{	
    	        		tvc = (TextView)vi.findViewById(crsetv[i+1]);
    	        		tvc.setText(item.get(crn) + " - " + item.get(cname));
    	        		tvc.setVisibility(View.VISIBLE);
    	        		i++;
    	        		cname = i + "cname";
    	        		crn = i + "cCRN";
    	        	}
    	        }
            }
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_COURSES))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.courses_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.courses_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_EVENTS))
	    {
	    	int i = 0;
	    	//RadioButton b1;
        	for(i=0; i<ListActivity.events_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.events_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        		        		        			
        		/*b1 = (RadioButton)vi.findViewById(R.id.radioButton1);      		
        		b1.setVisibility(View.VISIBLE);
        		
        		if(ListActivity.selectedIndex != -1)
        		{
        			if(i == ListActivity.selectedIndex)
        			{
        				b1.setChecked(true);        				
        			}
        		}*/
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_NEWS))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.news_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.news_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_CONTACT))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.contact_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.contact_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_EVENTS))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.events_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.events_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_EVENTS))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.events_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.events_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_NEWS))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.news_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.news_content[i]));        	
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
	    else if(ListActivity.itemSelection.equals(MainActivity.ACTIVITY_CONTACT))
	    {
	    	int i = 0;
        	for(i=0; i<ListActivity.contact_content.length; i++)
        	{
        		tv[i] = (TextView)vi.findViewById(tvlist[i]);
        		tv[i].setText(item.get(ListActivity.contact_content[i]));        		
        		tv[i].setVisibility(View.VISIBLE);
        	}
	    }
        
        return vi;
    }
}

