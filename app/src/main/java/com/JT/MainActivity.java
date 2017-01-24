package com.JT;

import android.app.*;
import android.os.*;
import android.util.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.lang.reflect.*;
import android.view.*;

public class MainActivity extends Activity 
{
	
	public WebView mWebView;
	public LinearLayout LL;
	public ProgressBar PB;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		mWebView = new WebView(this);
		
		LL = (LinearLayout) findViewById(R.id.mainLinearLayout1);
		PB = (ProgressBar) findViewById(R.id.mainProgressBar1);
		
		mWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
													 
		// Enable Javascript
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		mWebView.setWebViewClient(new mWebViewClient());
		mWebView.setWebChromeClient(new mWebChromeClient());
		
		LL.addView(mWebView, 1);
		
		if(setProxy(mWebView, "118.98.216.122", 8080, null) == true){
			mWebView.loadUrl("https://whatismyipaddress.com");
		}else{
			
		}
		
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
			super.onPageFinished(view, url);
		}
		
    }
	
}
