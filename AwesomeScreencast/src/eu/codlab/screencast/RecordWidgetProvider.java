package eu.codlab.screencast;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class RecordWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int []appWidgetId) {
    	 
        // Prepare widget views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_record);
 
        // Prepare intent to launch on widget click
		Intent serviceIntent = new Intent(context, AwesomeScreencastService.class);
		serviceIntent.putExtra("file", "file");
 
        // Launch intent on widget click
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
        views.setOnClickPendingIntent(R.src.go, pendingIntent);
 
        for(int i=0;appWidgetId != null && i<appWidgetId.length;i++)
        	appWidgetManager.updateAppWidget(appWidgetId[i], views);
    }}
