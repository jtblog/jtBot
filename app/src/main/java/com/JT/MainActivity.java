package com.JT;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class MainActivity extends Activity 
{

	public WebView mWebView;
	public LinearLayout LL;
	public ProgressBar PB;
	public static MainActivity mInstance;
	public int indx;
	public Handler mHander;
	public List<String> proxies;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mHander = new Handler();

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		mWebView = new WebView(this);
		indx = 0;

		LL = (LinearLayout) findViewById(R.id.mainLinearLayout1);
		PB = (ProgressBar) findViewById(R.id.mainProgressBar1);

		mWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		// Enable Javascript
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		mWebView.setWebViewClient(new mWebViewClient());
		mWebView.setWebChromeClient(new mWebChromeClient());

		LL.addView(mWebView, 1);
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

		startService(new Intent(getApplicationContext(), BotService.class));
		//new BotAsyncTask().execute();
		//mHander.post(new BotRunnable1(indx));
    }

	/*
	public static MainActivity getInstance(){
		if(mInstance == null){
			return new MainActivity();
		}
		return mInstance;
	}
	*/

	private static Object getFieldValueSafely(Field field, Object classInstance) throws IllegalArgumentException, IllegalAccessException {
		boolean oldAccessibleValue = field.isAccessible();
		field.setAccessible(true);
		Object result = field.get(classInstance);
		field.setAccessible(oldAccessibleValue);
		return result;
	}

	public WebView getWebView()
	{
		return mWebView;
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
			PB.setProgress(newProgress);
		}

	}

	public class mWebViewClient extends WebViewClient
	{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			// TODO: Implement this method
			PB.setVisibility(View.VISIBLE);
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			// TODO: Implement this method
			PB.setVisibility(View.INVISIBLE);
			//startService(new Intent(getApplicationContext(), BotService.class));
			super.onPageFinished(view, url);
			//indx++;
			//mHander.post(new BotRunnable(indx));
		}

    }

	public class BotRunnable implements Runnable
	{
		public int indx;

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
				if(port == 443 || port == 8080){
					if(setProxy(mWebView, proxy, port, null) == true){
						try{
							mWebView.loadUrl("https://jtblog.github.io");
						}catch(Exception e){}
					}else{
						Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
					}
				}else{
					indx++;
					mHander.post(this);
				}

				if(indx == proxies.size()){
					indx = 0;
				}

			}

		}
	}
	
	class BotAsyncTask extends AsyncTask<Void,Void,Void>
	{

		@Override
		protected Void doInBackground(Void[] p1)
		{
			// TODO: Implement this method
			
			return null;
		}
	}

}