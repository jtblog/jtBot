package com.JT;

import android.app.*;
import android.content.*;

public class BotReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context p1, Intent p2)
	{
		// TODO: Implement this method
		if(!isServiceRunning(BotService.class, p1) == true){
			p1.startActivity(new Intent(p1, MainActivity.class));
			//p1.startService(new Intent(p1, BotService.class));
		}
		
	}
	
	public boolean isServiceRunning(Class<?> serviceClass, Context p1){
        ActivityManager activityManager = (ActivityManager) p1.getSystemService(Context.ACTIVITY_SERVICE);

        // Loop through the running services
        for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                // If the service is running then return true
                return true;
            }
        }
        return false;
    }

}