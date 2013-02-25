package eu.codlab.screencast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class AwesomeScreencastService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	Handler handler = new Handler();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("onStartCommand","start");
		final String file = intent!= null ? intent.getStringExtra("file") : null;
		if(file != null){
			copyAssets();
			handler.post(new Runnable(){
				public void run(){

					Process p;
					try {
						Log.d("-","START");
						p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/mount -o remount,rw /system"});
						p.waitFor();
						p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/cat /data/data/eu.codlab.screencast/ffmpeg > /system/bin/ffmpeg"});
						p.waitFor();
						p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/chmod 755 /system/bin/ffmpeg"});
						p.waitFor();
						p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/mount -o remount,ro /system"});
						p.waitFor();
						createNotificationStop();
						p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/ffmpeg -y -f fbdev -i /dev/graphics/fb0 -r 24 "+file+" &>> /sdcard/log.txt"});
						Log.d("-","FINSH");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		}else if(intent != null && intent.getStringExtra("kill") != null){

			try{
				Process p =  Runtime.getRuntime().exec(new String[]{"su","-c","/system/xbin/killall -2 ffmpeg"});
				p.waitFor();
			} catch (Exception e) {
				createNotificationStop(e.getMessage());
				e.printStackTrace();
			}
			this.stopForeground(true);
			this.stopSelf();
		}


		super.onStartCommand(intent, flags, startId);

		return Service.START_STICKY;
	}

	private void createNotificationStop(String text,Intent intent){
		NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		final Notification notif = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getService(this, 42, intent, 0);

		notif.setLatestEventInfo(this, text,
				text, contentIntent);
		//notifManager.notify(42, notif);
		startForeground(42, notif);
	}

	private void createNotificationStop(String text){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		createNotificationStop(text, intent);
	}
	private void createNotificationStop(){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		intent.putExtra("kill", "dammit");
		createNotificationStop("Stop", intent);
	}

	private void copyAssets() {
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open("ffmpeg");
			out = new FileOutputStream("/data/data/eu.codlab.screencast/ffmpeg");
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch(IOException e) {
			Log.e("tag", "Failed to copy asset file: ffmpeg", e);
		}       
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}


}
