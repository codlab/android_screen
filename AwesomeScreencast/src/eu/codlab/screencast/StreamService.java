package eu.codlab.screencast;

import android.app.Service;
import android.content.Intent;
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
