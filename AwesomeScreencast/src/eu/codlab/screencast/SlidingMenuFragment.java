package eu.codlab.screencast;


import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;

public class SlidingMenuFragment extends SherlockFragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_awesome_screencast_behind_full, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@SuppressWarnings("deprecation")
	private void getPoint(Display display, Point size){
		size.x=display.getWidth();
		size.y=display.getHeight();		
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void getPoint13(Display display, Point size){
		display.getSize(size);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		SharedPreferences pref = getSherlockActivity().getSharedPreferences("file", 0);

		final EditText file_framerate = (EditText)view.findViewById(R.file.behind_file_framerate);
		file_framerate.setText(Integer.toString(pref.getInt(Constants.FILE_FRAMERATE, 24)));
		Button file_save = (Button)view.findViewById(R.file.behind_file_save);
		file_save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(getSherlockActivity() != null){
					Editor edit = getSherlockActivity().getSharedPreferences("file", 0).edit();
					edit.putInt(Constants.FILE_FRAMERATE, Integer.parseInt(file_framerate.getText().toString()));
					edit.commit();
				}
			}
		});

		pref = getSherlockActivity().getSharedPreferences("server", 0);
		int height = pref.getInt(Constants.SERVER_HEIGHT, -1);
		int width = pref.getInt(Constants.SERVER_WIDTH, -1);
		Display display = getSherlockActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if(Build.VERSION.SDK_INT < 13){
			getPoint(display, size);
		}else{
			getPoint13(display, size);
		}
		if(width == -1){
			width = size.x;
			Editor edit = getSherlockActivity().getSharedPreferences("server", 0).edit();
			edit.putInt(Constants.SERVER_WIDTH, width);
			edit.commit();
		}
		if(height == -1){
			height = size.y;
			Editor edit = getSherlockActivity().getSharedPreferences("server", 0).edit();
			edit.putInt(Constants.SERVER_HEIGHT, height);
			edit.commit();
		}

		final EditText server_framerate = (EditText)view.findViewById(R.server.behind_server_framerate);
		server_framerate.setText(Integer.toString(pref.getInt(Constants.SERVER_FRAMERATE, 24)));
		final EditText server_height = (EditText)view.findViewById(R.server.behind_server_video_height);
		server_height.setText(Integer.toString(height));
		final EditText server_width = (EditText)view.findViewById(R.server.behind_server_video_width);
		server_width.setText(Integer.toString(width));
		final EditText server_maxconn = (EditText)view.findViewById(R.server.behind_server_maxconnections);
		server_maxconn.setText(Integer.toString(pref.getInt(Constants.SERVER_MAXCONN, 5)));
		final EditText server_port = (EditText)view.findViewById(R.server.behind_server_listening_port);
		server_port.setText(Integer.toString(pref.getInt(Constants.SERVER_PORT, 8090)));

		Button server_save = (Button)view.findViewById(R.server.behind_server_save);
		server_save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(getSherlockActivity() != null){
					Editor edit = getSherlockActivity().getSharedPreferences("server", 0).edit();
					edit.putInt(Constants.SERVER_FRAMERATE, Integer.parseInt(server_framerate.getText().toString()));
					edit.putInt(Constants.SERVER_HEIGHT, Integer.parseInt(server_height.getText().toString()));
					edit.putInt(Constants.SERVER_MAXCONN, Integer.parseInt(server_maxconn.getText().toString()));
					edit.putInt(Constants.SERVER_PORT, Integer.parseInt(server_port.getText().toString()));
					edit.putInt(Constants.SERVER_WIDTH, Integer.parseInt(server_width.getText().toString()));
					edit.commit();
				}
			}
		});

		super.onViewCreated(view, savedInstanceState);
	}
}
