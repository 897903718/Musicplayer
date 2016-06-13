package chanlytech.musicplayer.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public class MyHttpClient {

	private static AsyncHttpClient asyncHttpClient;

	public static AsyncHttpClient getInstance(Context context) {
		if (asyncHttpClient == null) {
			asyncHttpClient = new AsyncHttpClient();
			//            PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
			//            asyncHttpClient.setCookieStore(myCookieStore);
			asyncHttpClient.setTimeout(12000);
		}
		return asyncHttpClient;
	}

	public static void post(String url, MyAsyncHttpResponseHandler handler) {
		post(url, null, handler);

	}
	public static void get(String url, MyAsyncHttpResponseHandler handler) {
       get(url,null, handler);
    }

	/** 
	 * post 请求 
	 *  
	 * @param url 
	 *            API 地址 
	 * @param params 
	 *            请求的参数 
	 * @param handler 
	 *            数据加载句柄对象 
	 */
	public static void post(String url, RequestParams params, MyAsyncHttpResponseHandler handler) {
		System.out.println("进入post");
		asyncHttpClient.post(url, params, handler);
	}
	/** 
	 * get 请求
	 *  
	 * @param url 
	 *            API 地址 
	 * @param params 
	 *            请求的参数 
	 * @param handler 
	 *            数据加载句柄对象 
	 */
	
	public static void get(String url, RequestParams params, MyAsyncHttpResponseHandler handler) {
		System.out.println("进入get");
		asyncHttpClient.get(url, params, handler);
    }
	
//	public static void http(String url, RequestParams params, MyAsyncHttpResponseHandler handler,String method  ){
//		if(method.equals("GET")){
//			asyncHttpClient.get(url, params, handler);
//		}else if(method.equals("POST")){
//			asyncHttpClient.post(url, params, handler);
//		}
//	}
}
