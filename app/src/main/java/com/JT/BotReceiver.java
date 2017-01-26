package com.JT;

import android.content.*;

public class BotReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context p1, Intent p2)
	{
		// TODO: Implement this method
		p1.startService(new Intent(p1, BotService.class));
	}

}