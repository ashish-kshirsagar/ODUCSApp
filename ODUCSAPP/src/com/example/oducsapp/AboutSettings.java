package com.example.oducsapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AboutSettings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_settings);	
		
		if(MainActivity.FilePath != null)
		{
			EditText fp = (EditText)findViewById(R.id.filePath);
			fp.setText(MainActivity.FilePath);
		}			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about_settings, menu);
		return true;
	}
	
	public void updatePath(View v)
	{
		EditText fp = (EditText)findViewById(R.id.filePath);
		
		if(fp.getText().toString().equals(""))
			MainActivity.FilePath = fp.getHint().toString();
		else
			MainActivity.FilePath = fp.getText().toString();
		
		finish();
	}
}
