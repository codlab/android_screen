package eu.codlab.screencast;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class StreamWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int []appWidgetId) {
    	 
        // Prepare widget views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stream);
 
        // Prepare intent to launch on widget click
		Intent serviceIntent = new Intent(context, StreamService.class);
		serviceIntent.putExtra("server", "server");
 
        // Launch intent on widget click
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
        views.setOnClickPendingIntent(R.src.go_server, pendingIntent);

        for(int i=0;appWidgetId != null && i<appWidgetId.length;i++)
        	appWidgetManager.updateAppWidget(appWidgetId[i], views);
    }}
