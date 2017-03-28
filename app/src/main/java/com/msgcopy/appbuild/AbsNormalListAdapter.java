package com.msgcopy.appbuild;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 15-7-15.
 */
public abstract class AbsNormalListAdapter<T> extends BaseAdapter {

    private List<T> datas = new ArrayList<T>();

    public void setDatas(List<T> datas){
        if (null != datas){
            this.datas.clear();
            this.datas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    public void appendDatas(List<T> datas){
        if (null != datas){
            this.datas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return this.datas.size();
    }

    @Override
    public T getItem(int position) {
        return this.datas.get(position);
    }

}
