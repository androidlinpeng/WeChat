package com.msgcopy.kaoke.a10668.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.msgcopy.appbuild.ResultData;
import com.msgcopy.appbuild.ResultManager;
import com.msgcopy.appbuild.Utils.ToastUtils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

	private IWXAPI api;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.api=WXAPIFactory.createWXAPI(this, com.msgcopy.appbuild.third.ThirdConstants.WECHAT_APP_ID, false);
		this.api.handleIntent(getIntent(), this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		this.api.handleIntent(getIntent(), this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onReq(BaseReq arg0) {
	}

	@Override
	public void onResp(BaseResp arg0) {
//		LogUtil.i("", arg0.getType() + "");
//		LogUtil.i("", arg0.openId);
		// getType 是 2 为分享动作
		if(arg0 instanceof SendAuth.Resp){
			SendAuth.Resp resp=(SendAuth.Resp)arg0;
			if(resp.errCode!=0){
				ToastUtils.showShort(getApplicationContext(), "授权取消");
				finish();
			}else{
				com.msgcopy.appbuild.third.WeChatManager.setCode(resp.code);
				new WXEntryTask().execute();
			}
		}else{
			finish();
		}
	}
	
	private class WXEntryTask extends AsyncTask<Object, Void, ResultData> {
		
		public WXEntryTask(){

		}

		@Override
		protected ResultData doInBackground(Object... params) {
			return com.msgcopy.appbuild.third.WeChatManager.fetchToken();
		}
		
		@Override
		protected void onPostExecute(ResultData data) {
			super.onPostExecute(data);
			if(!isFinishing()){
				if(ResultManager.isOk(data)){
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("third_wechat_get_token_success"));
				}else{
					ToastUtils.showShort(getApplicationContext(), "获取授权信息失败");
				}
				finish();
			}
		}
		
	}

}
