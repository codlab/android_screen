package eu.codlab.screencast;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AwesomeScreencastActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_awesome_screencast);
		
		TextView text = (TextView)findViewById(R.src.file);
		if(this.getSharedPreferences("file", 0) != null){
			String file = getSharedPreferences("file",0).getString("file", null);
			if(file == null)
				file="/sdcard/exemple.avi";
			text.setText(file);
		}
		Button go = (Button)findViewById(R.src.go);
		go.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent serviceIntent = new Intent(AwesomeScreencastActivity.this, AwesomeScreencastService.class);
				
				String new_file = ((TextView)findViewById(R.src.file)).getText().toString();
				getSharedPreferences("file",0).edit().putString("file", new_file).commit();
				serviceIntent.putExtra("file", new_file);
				startService(serviceIntent);
				finish();
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_awesome_screencast, menu);
		return false;
	}

}
