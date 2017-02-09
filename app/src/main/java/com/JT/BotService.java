package com.JT;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.webkit.*;
import android.widget.*;
import info.guardianproject.netcipher.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.net.ssl.*;

import android.net.Proxy;

public class BotService extends Service
{

	public WebView mWebView;
	public static int indx;
	public static Handler mHandler;
	public static List<String> proxies;
	public static List<String> userAgents;
	
	//private String testUrl = "http://correostrack.edgaruribe.mx/proxy.php";
	//public String testUrl = "https://api.ipify.org";

	@Override
	public void onDestroy()
	{
		// TODO: Implement this method
		Intent i = new Intent("BotAction0");
		sendBroadcast(i);
		if(!isServiceRunning(BotService.class, getApplicationContext()) == true){
			startService(new Intent(getApplicationContext(), BotService.class));
		}
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

		indx = 0;
		mHandler = new Handler();
		mWebView = new WebView(getApplicationContext());

		//mWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		// Enable Javascript
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		mWebView.setWebViewClient(new mWebViewClient());
		
		proxies = new ArrayList<String>();
		userAgents = new ArrayList<String>();

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

				while( (line = bReader.readLine()) != null){
					proxies.add(line);
				}
			} catch (IOException e) {

			}

			//mWebView.loadUrl("https://jtblog.github.io");

		}else{

		}

		LoadAgents();
		
		if(proxies.size() > 0){
			
			String[] params = proxies.get(indx).split(":");
			String proxy = params[0];
			int port = Integer.parseInt(params[1]);
			
			Boolean sp = setProxy(mWebView, proxy, port, null);
			mWebView.getSettings().setUserAgentString(userAgents.get(indx));
			
			Toast.makeText(getApplicationContext(), "Bot Service Started", Toast.LENGTH_SHORT).show();
			mWebView.loadUrl("https://jtblog.github.io");
			mHandler.post(new BotRunnable1());
		}else{
			mHandler.postDelayed(new BotRunnable0(), 10000);
		}
		
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

	public void LoadAgents()
	{
		this.userAgents.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 5.0; Windows NT 5.1; .NET CLR 1.1.4322)");
		this.userAgents.add("Opera/9.20 (Windows NT 6.0; U; en)");
		this.userAgents.add("Opera/9.00 (Windows NT 5.1; U; en)");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; en) Opera 8.50");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; en) Opera 8.0");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 7.02 [en]");
		this.userAgents.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20060127 Netscape/8.1");
		this.userAgents.add("Googlebot/2.1 ( http://www.googlebot.com/bot.html) ");
		this.userAgents.add("msnbot-Products/1.0 (+http://search.msn.com/msnbot.htm) ");
		this.userAgents.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/48 (like Gecko) Safari/48");
		this.userAgents.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; es) AppleWebKit/51 (like Gecko) Safari/51");
		this.userAgents.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; fr) AppleWebKit/85.7 (KHTML, like Gecko) Safari/85.5");
		this.userAgents.add("Mozilla/4.0 (compatible; Netcraft Web Server Survey)");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 5.0; Linux 2.4.18-4GB i386) Opera 6.0 [en]");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322)");
		this.userAgents.add("Mozilla/5.0 (Windows; U; Win 9x 4.90; rv:1.7) Gecko/20041103 Firefox/0.9.3");
		this.userAgents.add("Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7");
		this.userAgents.add("Mozilla/5.0 (Windows; U; Windows NT 6.1; ja; rv:1.9.2a1pre) Gecko/20090403 Firefox/3.6a1pre");
		this.userAgents.add("Mozilla/4.0 (compatible; MSIE 5.0; Windows XP) Opera 6.04 [fr]");
		this.userAgents.add("Opera/9.61 (Windows NT 6.1; U; zh-cn) Presto/2.1.1");
		this.userAgents.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10_4_11; ar) AppleWebKit/525.18 (KHTML, like Gecko) Version/3.1.1 Safari/525.18");
		this.userAgents.add("Mozilla/5.0 (Windows; U; Windows NT 6.1; es-AR; rv:1.9) Gecko/2008051206 Firefox/3.0");
	}

	public String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }
        return response.toString();
    }

	public class BotRunnable0 implements Runnable
	{

		public BotRunnable0(){

		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			Toast.makeText(getApplicationContext(), "Proxy List is empty", Toast.LENGTH_SHORT).show();
			
			mHandler.postDelayed(new BotRunnable0(), 10000);
			
		}
	}
	
	public class BotRunnable1 implements Runnable
	{
		//String p = "";

		public BotRunnable1(){//String prox){
			//p = prox;
		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			Random r = new Random();
			int i = r.nextInt(proxies.size() - 1);
			
			 	String[] params = proxies.get(i).split(":");
			 	String proxy = params[0];
			 	int port = Integer.parseInt(params[1]);

			 	HttpsURLConnection connection = null;
			 	try
				{
			 		String adr0 = "https://api.ipify.org";
			 		String adr1 = "https://jtblog.github.io/";

			 		//NetCipher NC0 = new NetCipher();
			 		URL url = new URL(adr1);
			 		NetCipher.setProxy(proxy, port);
			 		connection = NetCipher.getHttpsURLConnection(url);
					
					Random r1 = new Random();
					int indx1 = r1.nextInt(userAgents.size() - 1);
					connection.setRequestProperty("User-Agent", userAgents.get(indx1));
					
			 		//connection.setReadTimeout(10000);
			 		//connection.setConnectTimeout(10000);
			 		connection.setRequestMethod("GET");
			 		//connection.setDoInput(true);

			 		// Connect
			 		connection.connect();

			 		if(!(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)){
			 			Toast.makeText(getApplicationContext(), proxy + ": failed", Toast.LENGTH_LONG).show();
			 		}else{
						String con = readStream(connection.getInputStream());
						//BA1.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, new String[] { proxies.get(indx) } );
			 			
					}
				}catch (IOException e)
			 	{
			 		if(connection != null){
			 			connection.disconnect();
			 			connection = null;
				 	}
			 	}
				mHandler.post(this);
		}
	}
	
	public class mWebViewClient extends WebViewClient
	{

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
		{
			// TODO: Implement this method
			Random r = new Random();
			indx = r.nextInt(proxies.size() - 1);
			
			//Toast.makeText(getApplicationContext(), String.valueOf(indx), Toast.LENGTH_SHORT).show();
			String[] params = proxies.get(indx).split(":");
			String proxy = params[0];
			int port = Integer.parseInt(params[1]);

			setProxy(mWebView, proxy, port, null);
			
			Random r1 = new Random();
			int indx1 = r1.nextInt(userAgents.size() - 1);
			view.getSettings().setUserAgentString(userAgents.get(indx1));

			view.loadUrl("https://jtblog.github.io");
			
			//super.onReceivedError(view, request, error);
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
			
			Random r = new Random();
			indx = r.nextInt(proxies.size() - 1);

			//Toast.makeText(getApplicationContext(), String.valueOf(indx), Toast.LENGTH_SHORT).show();
			String[] params = proxies.get(indx).split(":");
			String proxy = params[0];
			int port = Integer.parseInt(params[1]);

			setProxy(mWebView, proxy, port, null);

			Random r1 = new Random();
			int indx1 = r1.nextInt(userAgents.size() - 1);
			view.getSettings().setUserAgentString(userAgents.get(indx1));
			
			view.loadUrl(url);
			
			
			/*
			new Thread(new Runnable() {
					public void run() {
						
					}
				}).start();
			*/
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