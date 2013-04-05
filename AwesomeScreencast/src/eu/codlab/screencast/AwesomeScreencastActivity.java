package eu.codlab.screencast;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class AwesomeScreencastActivity extends SlidingFragmentActivity {

	public static final String exampleAvi = "screencast.avi";
	public static final File sdCard = Environment.getExternalStorageDirectory();
	public static final File exampleAviPath = new File(sdCard, exampleAvi);
	private TextView text;
	private boolean exitingChooser;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_awesome_screencast);
		
		this.setBehindContentView(R.layout.activity_awesome_screencast_behind);
		
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		t.replace(R.main.menufragment, new SlidingMenuFragment());
		t.commit();

		text = (TextView)findViewById(R.src.file);
		if(this.getSharedPreferences("file", 0) != null){
			String file = getSharedPreferences("file",0).getString("file", null);
			if(file == null) file = exampleAviPath.getAbsolutePath();
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
		Log.v("SlidingMenu", "exitingChooser: " + exitingChooser);
		// Do not show SlidingMenu if exiting from the folder-chooser
		if (!exitingChooser) {
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
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_awesome_screencast, menu);
		return false;
	}
	
	// long-press to select FOLDERs only
    private static final int _ReqChooseFile = 0;
	
    // onClick="openFilechooser" from layouts xml
	public void openFilechooser(View view) {
		Intent intent = new Intent(this,  FileChooserActivity.class);
		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
		intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
		startActivityForResult(intent, _ReqChooseFile);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		exitingChooser = true;
        switch (requestCode) {
        case _ReqChooseFile:
            if (resultCode == RESULT_OK) {
                @SuppressWarnings("unchecked")
				List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
                	
            	File chooserFolder = files.get(0);
            	getSharedPreferences("file",0).edit().putString("CHOOSER_FOLDER", chooserFolder.getAbsolutePath()).commit();
            	String chooserFilePath = new File(chooserFolder, exampleAvi).getAbsolutePath();
            	text.setText(chooserFilePath);
            	getSharedPreferences("file",0).edit().putString("file", chooserFilePath).commit();
            	Log.d("FILE_CHOOSER", "file-chooser selection: " + chooserFolder.getAbsolutePath());
            }
            break;
        }
    }
}
