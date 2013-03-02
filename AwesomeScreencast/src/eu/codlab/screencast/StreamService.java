package eu.codlab.screencast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

public class StreamService extends Service{
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("onStartCommand Stream",intent.toString());
		Intent serviceIntent = new Intent(this, AwesomeScreencastService.class);
		serviceIntent.putExtra("server", "");
		startService(serviceIntent);
		
		this.stopSelf();

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
