package com.msgcopy.appbuild.third;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.msgcopy.R;
import com.msgcopy.appbuild.Utils.CommonUtil;
import com.msgcopy.appbuild.Utils.LogUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liang on 2017/3/2.
 */
public class ShareWechatTask extends AsyncTask<Object,Void,Void>{

    private Activity activity = null;

    private IWXAPI iwxapi = null;

    public ShareWechatTask(Activity activity) {
        this.activity = activity;
        this.iwxapi= WXAPIFactory.createWXAPI(activity.getApplicationContext(), ThirdConstants.WECHAT_APP_ID, false);
        this.iwxapi.registerApp(ThirdConstants.WECHAT_APP_ID);
    }

    @Override
    protected Void doInBackground(Object... params) {
        int scene = (Integer) params[0];
        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
        String imgUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488439701789&di=8f225e847808fb9cf98b6865f8d439ec&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F17%2F14%2F25%2F43Y58PICfJB_1024.jpg";
        if (!CommonUtil.isBlank(imgUrl)) {
            try {
                URL url = new URL(imgUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setReadTimeout(10 * 1000);
                conn.setConnectTimeout(10 * 1000);

                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                bitmap = BitmapFactory.decodeStream(bis, null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = "http://blog.csdn.net/hello_1s/article/details/52636447";
        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.mediaObject = webpageObject;
        mediaMessage.title = "微信分享";
        mediaMessage.description = "分享内容";
        int bitmapSize = bitmap.getRowBytes() * bitmap.getHeight();
        LogUtil.i("bitmapSize", bitmapSize + "");
        mediaMessage.setThumbImage(bitmap);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = mediaMessage;
        req.scene = scene;
        req.transaction = String.valueOf(System.currentTimeMillis());

        if (!isCancelled()) {
            iwxapi.sendReq(req);
        }
        return null;
    }
}
