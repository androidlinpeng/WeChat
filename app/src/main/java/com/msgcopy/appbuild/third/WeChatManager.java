package com.msgcopy.appbuild.third;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.msgcopy.appbuild.Http;
import com.msgcopy.appbuild.MsgApplication;
import com.msgcopy.appbuild.ResultData;
import com.msgcopy.appbuild.ResultManager;
import com.msgcopy.appbuild.Utils.CommonUtil;
import com.msgcopy.appbuild.Utils.ToastUtils;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by liang on 2017/2/28.
 */
public class WeChatManager {

    private static final String SP_WECHAT_INFO = "third_wechat_info";
    private static final String SP_WECHAT_OPENID = "third_wechat_openid";
    private static final String SP_WECHAT_TOKEN = "third_wechat_token";
    private static final String SP_WECHAT_CODE = "third_wechat_code";
    private static final String SP_WECHAT_NICKNAME = "third_wechat_nickname";


    public static final String URL_GET_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    public static final String URL_GET_USER_INFO = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";


    public static void startAuth(Context cxt) {
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(cxt, ThirdConstants.WECHAT_APP_ID, false);
        iwxapi.registerApp(ThirdConstants.WECHAT_APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        iwxapi.sendReq(req);
    }

    public static ResultData fetchUserInfo() {
        String url = String.format(URL_GET_USER_INFO, getToken(), getOpenid());
        ResultData data = Http.get(url);
        if (ResultManager.isOk(data)) {
            try {
                JSONObject json = new JSONObject((String) data.getData());
                String errorCode = json.optString("errcode");
                if (!CommonUtil.isBlank(errorCode)) {
                    data = ResultManager.createFailData("授权失败");
                } else {
                    setNickname(json.optString("nickname"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static ResultData fetchToken() {
        String code = getCode();
        String url = String.format(URL_GET_TOKEN, ThirdConstants.WECHAT_APP_ID, ThirdConstants.WECHAT_APP_SECRET, code);
        ResultData data = Http.get(url);
        if (ResultManager.isOk(data)) {
            try {
                JSONObject json = new JSONObject((String) data.getData());
                String errorCode = json.optString("errcode");
                if (!CommonUtil.isBlank(errorCode)) {
                    data = ResultManager.createFailData("授权失败");
                } else {
                    setToken(json.optString("access_token"));
                    setOpenid(json.optString("openid"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void setCode(String code) {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_CODE, code).apply();
    }

    public static String getCode() {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_CODE, "");
    }

    public static void setNickname(String nickname) {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_NICKNAME, nickname).apply();
    }

    public static String getNickname() {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_NICKNAME, "");
    }

    public static void setToken(String token) {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_TOKEN, token).apply();
    }

    public static String getToken() {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_TOKEN, "");
    }

    public static void setOpenid(String openid) {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_OPENID, openid).apply();
    }

    public static String getOpenid() {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_OPENID, "");
    }

    public static ResultData bindWechat() {
        String openId = getOpenid();
        ResultData data = null;
        if (!CommonUtil.isBlank(openId)) {
            try {
                data = fetchUserInfo();
                if (ResultManager.isOk(data)) {

                    String wechatUserJson = (String) data.getData();
                    JSONObject userJson = new JSONObject(wechatUserJson);
                    // 更新firstname
                    String fName = userJson.optString("nickname");
                    // 更新个人信息
                    String gender = userJson.optString("sex");
                    if (gender.equals("1")) {
                        gender = "true";
                    } else if (gender.equals("2")) {
                        gender = "false";
                    } else {
                        gender = "false";
                    }
                    String prov = userJson.optString("province");
                    String city = userJson.optString("city");
                    HashMap<String, String> values = new HashMap<String, String>();
                    values.clear();
                    values.put("gender", gender);
                    values.put("prov", prov);
                    values.put("city", city);
                    // values.put("mood", mood);
                    // 更新头像
                    URL url = new URL(userJson.optString("headimgurl"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setReadTimeout(10 * 1000);
                    conn.setConnectTimeout(10 * 1000);
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    Bitmap bitmap = BitmapFactory.decodeStream(bis);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return data;
    }

    public static void reset() {
        SharedPreferences sp = MsgApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    public static void send(Activity activity, int scene) {
        ShareWechatTask asyncTask = new ShareWechatTask(activity);
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(activity.getApplicationContext(), ThirdConstants.WECHAT_APP_ID, false);
        iwxapi.registerApp(ThirdConstants.WECHAT_APP_ID);
        if (iwxapi.isWXAppInstalled()) {
            if (scene == SendMessageToWX.Req.WXSceneTimeline) {
                if (iwxapi.getWXAppSupportAPI() >= 0x21020001) {
                    asyncTask.execute(scene);
                } else {
                    ToastUtils.showShort(activity, "您的微信版本不支持朋友圈");
                }
            } else {
                asyncTask.execute(scene);
            }

        } else {
            ToastUtils.showShort(activity, "未安装微信客户端");
        }
    }
}
