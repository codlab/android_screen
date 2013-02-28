package eu.codlab.screencast;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AwesomeScreencastActivity extends SlidingFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_awesome_screencast);
		
		this.setBehindContentView(R.layout.activity_awesome_screencast_behind);
		
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		t.replace(R.main.menufragment, new SlidingMenuFragment());
		t.commit();

		TextView text = (TextView)findViewById(R.src.file);
		if(this.getSharedPreferences("file", 0) != null){
			String file = getSharedPreferences("file",0).getString("file", null);
			if(file == null)
				file="/sdcard/exemple.avi";
			text.setText(file);
		}
		
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setSlidingEnabled(true);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setBehindWidthRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.90f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		setSlidingActionBarEnabled(false);
		
		View go = findViewById(R.src.go);
		go.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent serviceIntent = new Intent(AwesomeScreencastActivity.this, AwesomeScreencastService.class);
				
				String new_file = ((TextView)findViewById(R.src.file)).getText().toString();
				getSharedPreferences("file",0).edit().putString(Constants.FILE_OUTPUT, new_file).commit();
				serviceIntent.putExtra("file", new_file);
				startService(serviceIntent);
				finish();
			}
			
		});
		
		View go_png = findViewById(R.src.go_png);
		go_png.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent serviceIntent = new Intent(AwesomeScreencastActivity.this, AwesomeScreencastService.class);
				
				String new_file = ((TextView)findViewById(R.src.file)).getText().toString();
				getSharedPreferences("file",0).edit().putString(Constants.FILE_OUTPUT, new_file).commit();
				serviceIntent.putExtra("file_png", new_file);
				startService(serviceIntent);
				finish();
			}
			
		});
		
		View go_server = findViewById(R.src.go_server);
		go_server.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent serviceIntent = new Intent(AwesomeScreencastActivity.this, AwesomeScreencastService.class);
				
				serviceIntent.putExtra("server", "");
				startService(serviceIntent);
				finish();
			}
			
		});
	}

	@Override
	public void onStart(){
		super.onStart();
		
		Thread t = new Thread(){
			public void run(){
				try{
					Thread.sleep(1000);
				}catch(Exception e){
				}
				runOnUiThread(new Runnable(){
					public void run(){
						if(false == getSlidingMenu().isMenuShowing())
							toggle();
					}
				});
			}
		};
		t.start();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_awesome_screencast, menu);
		return false;
	}

}
