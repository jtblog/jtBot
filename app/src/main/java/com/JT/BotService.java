package com.JT;

import android.app.*;
import android.content.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import info.guardianproject.netcipher.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.net.ssl.*;

public class BotService extends Service
{
	
	public WebView mWebView;
	public int indx;
	public Handler mHander;
	public List<String> proxies;

	@Override
	public void onDestroy()
	{
		// TODO: Implement this method
		Intent i = new Intent("BotAction0");
		sendBroadcast(i);
		//startService(new Intent(getApplicationContext(), BotService.class));
		super.onDestroy();
	}
	
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
		
		mHander = new Handler();

		mWebView = new WebView(getApplicationContext());
		indx = 0;

		mWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		// Enable Javascript
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		mWebView.setWebViewClient(new mWebViewClient());
		mWebView.setWebChromeClient(new mWebChromeClient());

		proxies = new ArrayList<String>();
		
		File baseDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getPackageName().toString() + "/");
		if(!baseDir.exists()){
			baseDir.mkdirs();
		}

		String fullpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getPackageName().toString() + "/proxies.txt";
		File p = new File(fullpath);

		if (p.exists()){

			String line = "";

			try {
				FileReader fReader = new FileReader(p);
				BufferedReader bReader = new BufferedReader(fReader);

				while( (line = bReader.readLine()) != null  ){
					proxies.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			//mWebView.loadUrl("https://jtblog.github.io");

		}else{

		}
		
		Toast.makeText(getApplicationContext(), "Bot Service Started", Toast.LENGTH_SHORT).show();
		
		//String[] params = proxies.get(2).split(":");
		//String proxy = params[0];
		//String port = params[1];
		
		mHander.post(new BotRunnable(indx));

		return START_STICKY;
	}
	
	private static Object getFieldValueSafely(Field field, Object classInstance) throws IllegalArgumentException, IllegalAccessException {
		boolean oldAccessibleValue = field.isAccessible();
		field.setAccessible(true);
		Object result = field.get(classInstance);
		field.setAccessible(oldAccessibleValue);
		return result;
	}

	/**
	 * Set Proxy for Android 4.1 and above.
	 */
	public boolean setProxy(WebView webview, String host, int port, String exclusionList) {

		try {

			Class wvcClass = Class.forName("android.webkit.WebViewClassic");
			Class wvParams[] = new Class[1];
			wvParams[0] = Class.forName("android.webkit.WebView");
			Method fromWebView = wvcClass.getDeclaredMethod("fromWebView", wvParams);           
			Object webViewClassic = fromWebView.invoke(null, webview);      

			Class wv = Class.forName("android.webkit.WebViewClassic");
			Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
			Object mWebViewCoreFieldIntance = getFieldValueSafely(mWebViewCoreField, webViewClassic);

			Class wvc = Class.forName("android.webkit.WebViewCore");
			Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
			Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldIntance);

			Class bf = Class.forName("android.webkit.BrowserFrame");
			Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
			Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

			Class ppclass = Class.forName("android.net.ProxyProperties");
			Class pparams[] = new Class[3];
			pparams[0] = String.class;
			pparams[1] = int.class;
			pparams[2] = String.class;
			Constructor ppcont = ppclass.getConstructor(pparams);

			Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
			Class params[] = new Class[1];
			params[0] = Class.forName("android.net.ProxyProperties");
			Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

			updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, exclusionList));

		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	public class mWebChromeClient extends WebChromeClient
	{

		@Override
		public void onProgressChanged(WebView view, int newProgress)
		{
			// TODO: Implement this method
			super.onProgressChanged(view, newProgress);
		}

	}

	public class mWebViewClient extends WebViewClient
	{

		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
		{
			// TODO: Implement this method
			indx++;
			mHander.post(new BotRunnable(indx));
			super.onReceivedHttpError(view, request, errorResponse);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			// TODO: Implement this method
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			// TODO: Implement this method
			//startService(new Intent(getApplicationContext(), BotService.class));
			super.onPageFinished(view, url);
			indx++;
			mHander.post(new BotRunnable(indx));
		}

    }
	
	public class BotRunnable implements Runnable
	{
		public int indx;
		//public String pproxies;
		
		public BotRunnable(int i){
			indx = i;
		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			if(proxies.size() > 0 && !proxies.get(0).trim().equalsIgnoreCase("")){

				String[] params = proxies.get(indx).split(":");
				String proxy = params[0];
				int port = Integer.parseInt(params[1]);

				try
				{
					URL url = new URL("https://jtblog.github.io/");
					NetCipher.setProxy(proxy, port);
					HttpsURLConnection connection = NetCipher.getHttpsURLConnection(url);

					connection.setReadTimeout(10000);
					connection.setConnectTimeout(10000);
					connection.setRequestMethod("GET");
					//connection.setDoInput(true);

					// Connect
					connection.connect();

					if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK){
						if(proxies.contains(proxies.get(indx))){
							proxies.remove(proxies.get(indx));
						}
					}else{
						Toast.makeText(getApplicationContext(), proxies.get(indx), Toast.LENGTH_LONG).show();
					}

					indx ++;
					connection.disconnect();

				}
				catch (IOException e)
				{
					indx ++;
				}

				if(indx == proxies.size() - 1){
					indx = 0;
				}

				mHander.post(new BotRunnable(indx));
			}
			
		}
	}
	
}