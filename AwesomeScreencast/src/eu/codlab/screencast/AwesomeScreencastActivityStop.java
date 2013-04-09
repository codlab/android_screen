package eu.codlab.screencast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AwesomeScreencastActivityStop extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_awesome_screencast);
		Intent serviceIntent = new Intent(AwesomeScreencastActivityStop.this, AwesomeScreencastService.class);
		serviceIntent.putExtra("kill", "");
		startService(serviceIntent);

		//finish();
	}
}
