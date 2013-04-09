package eu.codlab.screencast;

import java.io.File;
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
import java.util.concurrent.TimeoutException;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

public class AwesomeScreencastService extends Service{
	public static int STATE_RECORDING =0;
	public static int STATE_STOP=1;

	private final static String DEBUG_TAG = "SuCommand";
	static boolean BB = RootTools.isBusyboxAvailable();
	static boolean SU = RootTools.isRootAvailable();
	
	private static int _state = STATE_STOP;
	private String defaultDestFile = AwesomeScreencastActivity.exampleAviPath.getAbsolutePath();
	private String defaultDestFolder = AwesomeScreencastActivity.sdCard.getAbsolutePath();
	private static Context nContext;
	
	public static Context getContext() {
        return nContext;
    }

	private void setState(int state){
		_state = state;
	}

	private int getState(){
		return _state;
	}

	private boolean isRecording(){
		return AwesomeScreencastService.STATE_RECORDING == getState();
	}

	private String getLogDestination(){
		String logFolder = this.getSharedPreferences("file", 0).getString("CHOOSER_FOLDER", defaultDestFolder);
		File log = new File(logFolder, "log.txt");
		return log.getAbsolutePath();
	}

	private String getFileOutput(){
		return this.getSharedPreferences("file", 0).getString(Constants.FILE_OUTPUT, defaultDestFile);
	}
	private int getListeningPort(){
		return this.getSharedPreferences("server", 0).getInt(Constants.SERVER_PORT, 8090);
	}

	private int getMaximumConnections(){
		return this.getSharedPreferences("server", 0).getInt(Constants.SERVER_MAXCONN, 5);
	}

	/*private int getMaxClients(){
		return this.getSharedPreferences("server", 0).getInt("maxclients", 1000);
	}

	private int getMaxBandWidth(){
		return this.getSharedPreferences("server", 0).getInt("maxbandwidth", 1000);
	}*/

	private int getVideoWidth(){
		int width = this.getSharedPreferences("server", 0).getInt(Constants.SERVER_WIDTH, 160) >> 1;
		if(width % 16 != 0){
			if(width < 16)
				return 16;
			return width - width % 16;
		}
		return width;
	}

	private int getVideoHeight(){
		int height = this.getSharedPreferences("server", 0).getInt(Constants.SERVER_HEIGHT, 160) >> 1;
			if(height % 16 != 0){
				if(height < 16)
					return 16;
				return height - height % 16;
			}
			return height;
	}

	private int getServerFramerate(){
		return this.getSharedPreferences("server", 0).getInt(Constants.SERVER_FRAMERATE, 24);
	}

	private int getFileFramerate(){
		return this.getSharedPreferences("file", 0).getInt(Constants.FILE_FRAMERATE, 24);
	}


	private String getLocalhostURI(){
		return "http://localhost:"+getListeningPort()+"/live.ffm";
	}

	private String getLocalIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return "127.0.0.1";
	}

	private String getUrl(){
		return "http://"+getLocalIp()+":"+this.getListeningPort()+"/live.mpeg";
	}

	private void createConf() throws FileNotFoundException{
		FileOutputStream out = this.openFileOutput("ffserver.conf", 0);
		OutputStreamWriter buf = new OutputStreamWriter(out);
		String str ="Port "+this.getListeningPort()+"\n";
		str+="BindAddress 0.0.0.0\n";
		str+="MaxHTTPConnections "+this.getMaximumConnections()+"\n";
		//str+="MaxClients "+this.getMaxClients()+"\n";
		str+="CustomLog -\n";
		str+="NoDaemon\n";
		str+="<Feed live.ffm>\n";
		str+="File /cache/live.ffm\n";
		str+="FileMaxSize 40M\n";
		str+="NoAudio\n";
		str+="ACL allow 127.0.0.1\n";
		str+="</Feed>\n";

		str+="<Stream live.mpeg>\n";
		str+="Feed live.ffm\n";
		str+="Format mpeg\n";
		str+="NoAudio\n";
		str+="VideoBitRate 100\n";
		str+="VideoFrameRate "+this.getServerFramerate()+"\n\n";
		str+="VideoBufferSize 10000\n\n";
		str+="VideoSize "+(this.getVideoWidth())+"x"+(this.getVideoHeight())+"\n";
		str+="# quality ranges - 1-31 (1 = best, 31 = worst)\n";
		str+="VideoQMin 1\n";
		str+="VideoQMax 15\n";
		//str+="VideoGopSize 10\n";
		str+="</Stream>\n";
		try {
			buf.write(str, 0, str.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(buf != null)buf.close();
			if(out != null)out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		nContext = getBaseContext();
	}
	
	Handler handler = new Handler();
	private Builder mBuilder;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("receiving command",startId+"");
		if(intent != null && intent.getStringExtra("file") != null){
			Log.d("onStartCommand","file");
			copyAssets();
			if(isRecording() == false){
				setState(STATE_RECORDING);
				handler.post(new Runnable(){
					public void run(){
						RootTools.debugMode = true;
						Log.d("-","START");
						suCmd("/system/bin/mount -o remount,rw /system");
						
						suCmd("/system/bin/cat /data/data/eu.codlab.screencast/ffmpeg > /system/bin/ffmpeg");
						
						suCmd("/system/bin/chmod 700 /system/bin/ffmpeg");
						
						suCmd("/system/bin/mount -o remount,ro /system");
						
						createNotificationStop();
						
						String params = "-c:v mpeg4 -vtag xvid -q:v 1 -r " + getFileFramerate(); // HQ xvid
						//String params = "-c:v rawvideo -r " + getFileFramerate(); // non-compressed raw video
						
						suCmd("/system/bin/ffmpeg -y -f fbdev -i /dev/graphics/fb0 " + params + " " + getFileOutput() + " &>> " + getLogDestination());
						Log.d("-","FINSH");
					}
				});

			}else{
				kill();
			}
		}else if(intent != null && intent.getStringExtra("file_png") != null){
			Log.d("onStartCommand","file_png");
			if(isRecording() == false){
				setState(STATE_RECORDING);
				copyAssets();
				handler.post(new Runnable(){
					public void run(){
						Log.d("-","START");
						suCmd("[ ! -f /system/bin/ffmpeg ] && ( /system/bin/mount -o remount,rw /system; /system/bin/cat /data/data/eu.codlab.screencast/ffmpeg > /system/bin/ffmpeg ; /system/bin/chmod 700 /system/bin/ffmpeg ; /system/bin/mount -o remount,ro /system )");
						createNotificationStopFrames();
						suCmd("mkdir /cache/pngs; let x=`date +%s`; while test \"1\" = \"1\"; do screencap -p /cache/pngs/img$x.png;let x=$x+1; done;");
						Log.d("-","FINSH");
					}
				});

			}else{
				kill();
			}
		}else if(intent != null && intent.getStringExtra("kill_png") != null){

			Log.d("onStartCommand","kill_png");
			try{
				createNotificationWaitingFFMPEG();
				suCmd("/system/xbin/killall -2 sh; cat /cache/pngs/img*.png | /system/bin/ffmpeg -y -f image2pipe -vcodec png -r 5 -i - -r 5 "+getFileOutput()+"; rm -rf /cache/pngs");
				kill();
			} catch (Exception e) {
				createNotificationStop(getString(R.string.service_fault), e.getMessage());
				e.printStackTrace();
			}
			this.stopForeground(true);
			this.stopSelf();
		}else if(intent != null && intent.getStringExtra("server") != null){
			Log.d("onStartCommand","server");
			if(isRecording() == false){
				setState(STATE_RECORDING);
				copyAssets();
				handler.post(new Runnable(){
					public void run(){

						Log.d("-","START");
						suCmd("[ ! -f /system/bin/ffmpeg ] && [ ! -f /system/bin/ffserver ] && ( /system/bin/mount -o remount,rw /system ; /system/bin/cat /data/data/eu.codlab.screencast/ffmpeg > /system/bin/ffmpeg"
								+" ; /system/bin/cat /data/data/eu.codlab.screencast/ffserver > /system/bin/ffserver "
								+"; /system/bin/chmod 700 /system/bin/ffmpeg ; /system/bin/chmod 700 /system/bin/ffserver "
								+"; /system/bin/mount -o remount,ro /system )");

						try {
							createConf();
						} catch (FileNotFoundException e) {
							Log.e("-","createConf error: " + e.getMessage());
							e.printStackTrace();
						}

						suCmd("/system/bin/ffserver -f /data/data/eu.codlab.screencast/files/ffserver.conf");
						suCmd("sleep 2");
						createNotificationStopStream();
						
						suCmd("/system/bin/ffmpeg -y -f fbdev -i /dev/graphics/fb0 -r "+getServerFramerate()+" "+getLocalhostURI()+" &>> " + getLogDestination());
						Log.d("-","FINSH");
					}
				});


			}else{
				kill();
			}
		}else if(intent != null && intent.getStringExtra("kill") != null){
			Log.d("onStartCommand","kill");
			kill();
		}else{
			if(isRecording()){
				kill();
			}
		}
		super.onStartCommand(intent, flags, startId);

		return Service.START_NOT_STICKY;
	}

	private void createNotificationStop(String text, String sub_text, Intent intent){
		
		mBuilder =  new NotificationCompat.Builder(nContext);
		mBuilder.setSmallIcon(R.drawable.notif)
				.setContentTitle(text)
				.setContentText(sub_text);

		PendingIntent contentIntent = PendingIntent.getService(this, 0, intent, 0);
		mBuilder.setContentIntent(contentIntent);
		
		startForeground(42, mBuilder.getNotification());
	}

	private void createNotificationStop(String text, String sub_text){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		createNotificationStop(text, sub_text, intent);
	}
	private void createNotificationStopStream(){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		intent.putExtra("kill", "dammit");
		createNotificationStop(getString(R.string.service_streaming), getUrl()+" - "+getString(R.string.stop_streaming_video));
	}
	private void createNotificationStop(){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		intent.putExtra("kill", "dammit");
		createNotificationStop(getString(R.string.service_recording), getString(R.string.stop_recording_video));
	}

	private void createNotificationWaitingFFMPEG(){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		createNotificationStop(getString(R.string.service_waiting), getString(R.string.service_waiting_ffmpeg), intent);
	}
	private void createNotificationStopFrames(){
		Intent intent = new Intent(this, AwesomeScreencastService.class);
		intent.putExtra("kill_png", "dammit");
		createNotificationStop(getString(R.string.service_recording), getString(R.string.stop_recording_frames), intent);
	}

	private void copyAssets() {
		copyFFMPEG();
		copyFFSERVER();
	}
	private void copyFFMPEG(){
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open("ffmpeg");
			out = new FileOutputStream(nContext.getApplicationInfo().dataDir + "/ffmpeg");
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

	private void copyFFSERVER(){
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open("ffserver");
			out = new FileOutputStream(nContext.getApplicationInfo().dataDir + "/ffserver");
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch(IOException e) {
			Log.e("tag", "Failed to copy asset file: ffserver", e);
		}       
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	private void kill(){
		setState(STATE_STOP);
		try{
			suCmd("/system/xbin/killall -2 ffmpeg");
			suCmd("/system/xbin/killall -9 ffserver");
		} catch (Exception e) {
			createNotificationStop(getString(R.string.service_fault), e.getMessage());
			e.printStackTrace();
		}
		this.stopForeground(true);
		this.stopSelf();
	}

	public static void suCmd(String cmd) {
		int res = 4;
		if (BB && SU) {
			CommandCapture command = new CommandCapture(0, cmd);
			Log.d(DEBUG_TAG, cmd);
			
			try {
				RootTools.debugMode = true;
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (InterruptedException e) {
				res = res - 1;
			} catch (IOException e) {
				res = res - 1;
			} catch (TimeoutException e) {
				res = res - 1;
			} catch (RootDeniedException e) {
				res = res - 1;
			} finally {
				if (res == 4) {
					Log.i(DEBUG_TAG, "su_command_ok");
				} else {
					Log.e(DEBUG_TAG, "error executing su command");
					//Toast.makeText(context, "error executing su command", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

}
