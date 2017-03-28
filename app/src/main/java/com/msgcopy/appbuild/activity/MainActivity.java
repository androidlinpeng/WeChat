package com.msgcopy.appbuild.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.RequestParams;
import com.msgcopy.R;
import com.msgcopy.appbuild.ResultData;
import com.msgcopy.appbuild.ResultManager;
import com.msgcopy.appbuild.Utils.CommonUtil;
import com.msgcopy.appbuild.WAPIHttp;
import com.msgcopy.appbuild.third.SharePanelView;
import com.msgcopy.appbuild.third.ThirdConstants;
import com.msgcopy.appbuild.third.WeChatManager;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SharePanelView.OnShareBtnClickListener {

    private final static int TASK_CHECK_THIRD_WECHAT = 1100;

    private SharePanelView sharePanelView = null;

    private WechatReceiver wxReceiver = null;

    private ImageView avatar = null;

    private TextView nickname = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.avatar = (ImageView) findViewById(R.id.avatar);
        this.nickname = (TextView) findViewById(R.id.nickname);

        this.sharePanelView = new SharePanelView(this);
        this.sharePanelView.setOnShareBtnClickListener(this);

        wxReceiver = new WechatReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(wxReceiver, new IntentFilter("third_wechat_get_token_success"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wxReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                this.sharePanelView.show();
                break;
            case R.id.button2:
                WeChatManager.startAuth(getApplicationContext());
                break;
            case R.id.button3:
                new WechatPayTask().execute("019183d2ff2011e687e200163e04390d","110");
                break;
        }
    }

    @Override
    public void onShareBtnClicked(String key) {

    }

    private class UserLoginTask extends AsyncTask<Object, Void, ResultData> {

        private int task = -1;

        public UserLoginTask(int task) {
            this.task = task;
        }

        @Override
        protected ResultData doInBackground(Object... params) {
            ResultData data = null;
            switch (this.task) {
                case TASK_CHECK_THIRD_WECHAT:
                    data = WeChatManager.bindWechat();
                    break;
                default:
                    break;
            }
            return data;
        }

        @Override
        protected void onPostExecute(ResultData data) {
            super.onPostExecute(data);
            if (ResultManager.isOk(data)) {
                switch (this.task) {
                    case TASK_CHECK_THIRD_WECHAT:
                        if (ResultManager.isOk(data)) {
                            try {
                                String wechatUserJson = (String) data.getData();
                                JSONObject userJson = new JSONObject(wechatUserJson);
                                String name = userJson.optString("nickname");
                                String headimgurl = userJson.optString("headimgurl");
                                nickname.setText(name);
                                if (!CommonUtil.isBlank(headimgurl)) {
                                    Glide.with(getApplication()).load(headimgurl).into(avatar);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // 微信支付
    private class WechatPayTask extends AsyncTask<String, Void, ResultData> {

        private PayReq req = null;
        private IWXAPI msgApi = null;

        public WechatPayTask() {
            this.msgApi = WXAPIFactory.createWXAPI(getApplication(), null);
            this.msgApi.registerApp(ThirdConstants.WECHAT_APP_ID);
        }

        @Override
        protected ResultData doInBackground(String... params) {

            //需要后端的支持
            String orderId = params[0];
            String orderPrice = params[1];
            HashMap<String, String> values = new HashMap<String, String>();
            values.put("out_trade_no", orderId);
            values.put("total_fee", String.valueOf((int) (Float.valueOf(orderPrice) * 100)));
            values.put("app", "10668");

            return WAPIHttp.post("http://cloudapp.kaoke.me/wapi/wxpay/", new RequestParams(values));
        }

        @Override
        protected void onPostExecute(ResultData data) {
            super.onPostExecute(data);
            if (ResultManager.isOk(data)) {
                try {
                    JSONObject json = new JSONObject((String) data.getData());
                    req = new PayReq();
                    req.appId = ThirdConstants.WECHAT_APP_ID;
                    req.partnerId = ThirdConstants.WECHAT_PARTNER_ID;
                    req.prepayId = json.optString("prepay_id");
                    req.packageValue = "Sign=WXPay";
                    req.nonceStr = json.optString("nonce_str");
                    req.timeStamp = json.optString("timestamp");
                    req.sign = json.optString("sign");
                    msgApi.sendReq(req);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class WechatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String action = intent.getAction();
                if (action.equals("third_wechat_get_token_success")) {
                    new UserLoginTask(TASK_CHECK_THIRD_WECHAT).execute();
                }
            }
        }
    }

}
