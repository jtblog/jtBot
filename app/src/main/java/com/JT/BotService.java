package com.JT;

import android.app.*;
import android.content.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;

public class BotService extends Service
{

	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO: Implement this method
		//WebView WV = MainActivity.getInstance().getWebView();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
}
