package com.JT;

import android.app.*;
import android.content.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;
import info.guardianproject.netcipher.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.net.ssl.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import android.graphics.*;

public class BotService extends Service
{

	public WebView mWebView;
	public int success;
	public Handler mHandler;
	public List<String> proxies;
	public List<String> userAgents;
	public List<String> checker;
	public String myhtml = "";

	public PowerManager.WakeLock wakeLock;

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

		mHandler = new Handler();
		mWebView = new WebView(getApplicationContext());

		proxies = new ArrayList<String>();
		userAgents = new ArrayList<String>();
		checker = new ArrayList<String>();

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

		}else{

		}

		LoadAgents();

		if(proxies.size() > 0){

			HttpsURLConnection connection = null;
			try
			{
				String adr0 = "https://api.ipify.org";
				String adr1 = "https://jtblog.github.io/";

				//NetCipher NC0 = new NetCipher();
				URL url = new URL(adr1);
				//NetCipher.setProxy(proxy, port);
				connection = NetCipher.getHttpsURLConnection(url);

				//connection.setRequestProperty("User-Agent", userAgents.get(indx1));
				//connection.setReadTimeout(10000);
				//connection.setConnectTimeout(10000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);

				// Connect
				connection.connect();

				if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK){
					String res = readStream(connection.getInputStream());

					Document doc = Jsoup.parse(res);
					Elements es = doc.select("div.Ads");

					String strg = 
						"<html><head><script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js'></script>" +
						"<script type='text/javascript'>" +
						"function adclick(){" +
						"$('*').click();" +
						"}" +
						"</" + "script></head><body>";

					for(int ei = 0; ei < es.size(); ei++){
						Element e = es.get(ei);
						strg = strg + e.outerHtml();
					}
					strg = strg + "</body></html>";
					myhtml = strg;

					success = 0;
					mHandler.post(new BotRunnable1(mWebView, myhtml, success));
				}else{

				}

				//Toast.makeText(getApplicationContext(), strg, Toast.LENGTH_SHORT).show();

			}catch (IOException e)
			{
				if(connection != null){
					connection.disconnect();
					connection = null;
				}
			}

			if(connection != null){
				connection.disconnect();
				connection = null;
			}

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
			//Toast.makeText(getApplicationContext(), "Proxy List is empty", Toast.LENGTH_SHORT).show();
			HttpURLConnection connection = null;
			try
			{
				String adr0 = "https://api.ipify.org";
				String adr1 = "https://jtblog.github.io/";
				String adr2 = "http://txt.proxyspy.net/proxy.txt";

				//NetCipher NC0 = new NetCipher();
				URL url = new URL(adr2);
				//NetCipher.setProxy(proxy, port);
				connection = NetCipher.getHttpURLConnection(url);

				//Random r1 = new Random();
				//int indx1 = r1.nextInt(userAgents.size() - 1);
				//connection.setRequestProperty("User-Agent", userAgents.get(indx1));

				//connection.setReadTimeout(10000);
				//connection.setConnectTimeout(10000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);

				// Connect
				connection.connect();

				String fullpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getPackageName().toString() + "/proxy0.txt";
				File d = new File(fullpath);
				d.createNewFile();

				final FileOutputStream fileOutputStream = new FileOutputStream(d);
				final byte buffer[] = new byte[16 * 1024];

				final InputStream inputStream = connection.getInputStream();

				int len1 = 0;
				while ((len1 = inputStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, len1);
				}

				fileOutputStream.flush();
				fileOutputStream.close();

				String line = "";

				try {
					FileReader fReader = new FileReader(d);
					BufferedReader bReader = new BufferedReader(fReader);

					String text = "";
					while( (line = bReader.readLine()) != null  ){
						text = text + line;
					}

					String pattern = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?";
					Pattern compiledPattern = Pattern.compile(pattern);
					Matcher matcher = compiledPattern.matcher(text);
					while (matcher.find()) {
						checker.add(matcher.group());
					}

					String fullpath0 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getPackageName().toString() + "/proxies.txt";
					File d0 = new File(fullpath0);
					d0.createNewFile();

					FileOutputStream stream = new FileOutputStream(d0);

					for(int i2 = 0; i2 < checker.size(); i2++){
						String s = checker.get(i2) + "\n";
						stream.write(s.getBytes());
					}

					stream.flush();
					stream.close();

					//	Toast.makeText(getApplicationContext(), checker.get(0), Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK){
					stopSelf();
				}else{
					mHandler.post(this);
				}

			}catch (IOException e)
			{
				if(connection != null){
					connection.disconnect();
					connection = null;
				}
				mHandler.post(this);
			}

		}
	}

	public class BotRunnable1 implements Runnable
	{
		//String p = "";
		public WebView webv;
		public String html;
		public int count;

		public BotRunnable1(WebView view, String site, int num){//String prox){
			//p = prox;
			webv = view;
			html = site;
			count = num;
		}

		@Override
		public void run()
		{
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
												"MyWakelockTag");
			wakeLock.acquire();

			// TODO: Implement this method
			Random r0 = new Random();
			int i0 = r0.nextInt(proxies.size() - 1);

			String proxy = proxies.get(i0);
			String[] params = proxy.split(":");
			String host = params[0];
			int port = Integer.parseInt(params[1]);

			Random r1 = new Random();
			int i1 = r1.nextInt(userAgents.size() - 1);
			String ua = userAgents.get(i1);

			webv.getSettings().setJavaScriptEnabled(true);
			webv.getSettings().setUserAgentString(ua);

			Boolean b = setProxy(webv, host, port, null);

			webv.setWebViewClient(new mWebViewClient0());

			Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

			Notification notification = new Notification.Builder(getApplicationContext())
				.setContentTitle("JT Bot Proxy " + count)
				.setContentText(proxy)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent)
				//.setTicker(getText(R.string.ticker_text))
				.build();

			startForeground(8673, notification);

			String baseUrl    = "https://jtblog.github.io";
			String data       = html;
			String mimeType   = "text/html";
			String encoding   = "UTF-8";
			String historyUrl = "https://jtblog.github.io";

			webv.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);

			if(proxies.size() > 0){
				//mHandler.postDelayed(this, 10000);
			}else{
				mHandler.postDelayed(new BotRunnable0(), 10000);
			}
		}
	}

	public class BotRunnable2 implements Runnable
	{

		public BotRunnable2(){

		}

		@Override
		public void run()
		{
			// TODO: Implement this method
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

					while( (line = bReader.readLine()) != null){
						proxies.add(line);
					}
				} catch (IOException e) {

				}

			}else{

			}

		}
	}

	public class mWebViewClient0 extends WebViewClient
	{
		boolean timeout;
		public mWebViewClient0(){
			timeout = true;
		}

		@Override
		public void onPageStarted(final WebView view, String url, Bitmap favicon)
		{
			// TODO: Implement this method
			new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							//e.printStackTrace();
						}
						if(timeout) {
							// do what you want
							view.stopLoading();
						}
					}
				}).start();

			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
		{
			// TODO: Implement this method
			mHandler.post(new BotRunnable1(view, myhtml, success));
			super.onReceivedSslError(view, handler, error);
		}

		@Override
		public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
		{
			// TODO: Implement this method
			mHandler.post(new BotRunnable1(view, myhtml, success));
			super.onReceivedHttpError(view, request, errorResponse);
		}

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
		{
			mHandler.post(new BotRunnable1(view, myhtml, success));
			super.onReceivedError(view, request, error);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			// TODO: Implement this method
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(final WebView view, String url)
		{
			// TODO: Implement this method
			timeout = false;
			ConnectivityManager CM 
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo NI = CM.getActiveNetworkInfo();
			if(NI.isConnected() && NI.isAvailable()){
				success = success + 1;
			}

			mHandler.post(new BotRunnable1(view, myhtml, success));
			mHandler.postDelayed(new BotRunnable3(view), 5000);

			super.onPageFinished(view, url);
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

	public class BotRunnable3 implements Runnable
	{

		public final WebView w_view;

		public BotRunnable3(final WebView view){
			w_view = view;
		}

		@Override
		public void run()
		{
			int max_x = w_view.getWidth();
			int max_y = w_view.getContentHeight();

			w_view.loadUrl("javascript: adclick()");

			while((max_y - w_view.getScrollY()) > 10){
				w_view.setScrollY(w_view.getScrollY() + 5);
			}

			//w_view = null;

		}
	}

}
