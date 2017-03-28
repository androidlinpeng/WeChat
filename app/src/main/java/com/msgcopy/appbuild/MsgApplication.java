package com.msgcopy.appbuild;

import android.app.Application;

import com.loopj.android.http.PersistentCookieStore;
import com.msgcopy.appbuild.Utils.LogUtil;

import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * Created by liang on 2017/2/28.
 */
public class MsgApplication extends Application {

    private static MsgApplication globalContext = null;

    private PersistentCookieStore cookieStore = null;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
        this.cookieStore = new PersistentCookieStore(this);
        Http.getSyncHttpClient().setCookieStore(cookieStore);
        Http.getuploadFileHttpClient().setCookieStore(cookieStore);
    }

    public static MsgApplication getInstance() {
        return globalContext;
    }

    public String getCookie() {
        String cookieStr = "";
        List<Cookie> cookies = cookieStore.getCookies();
        if (null != cookies && cookies.size() > 0) {
            for (Cookie cookie : cookies) {
                // 这里最后的domain不写值就正常，很奇怪
                cookieStr = cookie.getName() + "=" + cookie.getValue() + "; expires=" + (null == cookie.getExpiryDate() ? "" : cookie.getExpiryDate().toGMTString()) + "; path="
                        + cookie.getPath() + "; domain=";
                break;
            }
        }
        LogUtil.i("getCookie", cookieStr);
        return cookieStr;
    }
}
