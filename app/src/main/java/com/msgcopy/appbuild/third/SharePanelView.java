package com.msgcopy.appbuild.third;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.msgcopy.R;
import com.msgcopy.appbuild.AbsNormalListAdapter;
import com.msgcopy.appbuild.Utils.CommonUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liang on 2017/3/2.
 */
public class SharePanelView implements AdapterView.OnItemClickListener {

    public interface OnShareBtnClickListener{
        void onShareBtnClicked(String key);
    }

    private OnShareBtnClickListener onShareBtnClickListener = null;

    public void setOnShareBtnClickListener(OnShareBtnClickListener l){
        this.onShareBtnClickListener = l;
    }



    private final static int LAYOUT_RES = R.layout.view_share_panel;

    private Activity activity = null;

    private View rootView = null;

    private GridView gridView = null;

    private ShareAdapter adapter = null;

    private PopupWindow popupWindow = null;

    public SharePanelView(Activity activity) {
        this.activity = activity;
        this.adapter = new ShareAdapter();
        this.rootView = activity.getLayoutInflater().inflate(LAYOUT_RES,null);
        this.gridView = (GridView)rootView.findViewById(R.id.gridView);
        this.gridView.setOnItemClickListener(this);
        this.gridView.setAdapter(adapter);

        this.popupWindow = new PopupWindow(activity);
        this.popupWindow.setContentView(rootView);
        this.popupWindow.setWidth(-1);
        this.popupWindow.setHeight(-2);
        this.popupWindow.setFocusable(true);
        this.popupWindow.setOutsideTouchable(true);
        this.popupWindow.setBackgroundDrawable(activity.getResources().getDrawable(android.R.color.white));
    }

    public void show(){
        if (!popupWindow.isShowing()){
            this.popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM,0,0);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String key = adapter.getItem(position);
        if (key.equals("wechat")){
            WeChatManager.send(activity, SendMessageToWX.Req.WXSceneSession);
        }else if (key.equals("moment")){
            WeChatManager.send(activity, SendMessageToWX.Req.WXSceneTimeline);
        }
        popupWindow.dismiss();
    }

    private class ShareAdapter extends AbsNormalListAdapter<String>{

        private final static int ITEM_RES = R.layout.row_share_panel_item;

        public ShareAdapter() {
            List<String> item = new ArrayList<String>();
            if (!CommonUtil.isBlank(ThirdConstants.WECHAT_APP_ID)){
                item.add("wechat");
                item.add("moment");
            }
            setDatas(item);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null==convertView){
                convertView = LayoutInflater.from(parent.getContext()).inflate(ITEM_RES, parent, false);
            }
            ImageView icon = (ImageView)convertView.findViewById(R.id.wx_icon);
            TextView text = (TextView)convertView.findViewById(R.id.wx_text);
            String key = getItem(position);
            if (key.equals("wechat")){
                text.setText(R.string.str_wechat);
                icon.setImageResource(R.mipmap.ic_third_party_share_wechat);
            }else if(key.equals("moment")){
                text.setText(R.string.str_wechat_moment);
                icon.setImageResource(R.mipmap.ic_third_party_share_moment);
            }
            return convertView;
        }
    }
}
