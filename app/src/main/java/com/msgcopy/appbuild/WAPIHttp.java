package com.msgcopy.appbuild;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class WAPIHttp {

	private static SyncHttpClient wapiHttpClient=new SyncHttpClient();

    static{
        wapiHttpClient.setTimeout(15*1000);
    }

	public static ResultData get(String url){
        appendCookie();
		ResultData data=new ResultData();
		wapiHttpClient.get(url, new HttpResponseHandler(data,url));
		return data;
	}
	
	public static ResultData post(String url,RequestParams params){
        appendCookie();
		ResultData data=new ResultData();
		wapiHttpClient.post(url, params, new HttpResponseHandler(data,url));
		return data;
	}
	
	public static ResultData put(String url,RequestParams params){
        appendCookie();
		ResultData data=new ResultData();
		wapiHttpClient.put(url, params, new HttpResponseHandler(data,url));
		return data;
	}

    private static void appendCookie(){
        String cookieStr=MsgApplication.getInstance().getCookie();
        wapiHttpClient.addHeader("cookie", cookieStr);
    }
}
