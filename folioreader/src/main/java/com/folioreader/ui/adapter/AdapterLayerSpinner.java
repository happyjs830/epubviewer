package com.folioreader.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.ThemeConfig;
import com.folioreader.ui.activity.FolioActivity;

import javax.security.auth.login.LoginException;

public class AdapterLayerSpinner extends BaseAdapter {
    private final String[] mList;
    private final LayoutInflater mInflater;

    public AdapterLayerSpinner(Context context, String[] list, Config config) {
        mList = list;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(mList != null)
            return mList.length;
        else
            return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.item_setting_spinner_normal, parent, false);
        }

        //데이터세팅
        TextView fontText = convertView.findViewById(R.id.tvItemSpinner);
        fontText.setTextColor(ThemeConfig._settingFontCenterTextColor);
        fontText.setText(mList[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.item_setting_spinner_dropdown, parent, false);
        }

        //데이터세팅
        TextView fontText = convertView.findViewById(R.id.tvItemSpinner);
        fontText.setTextColor(ThemeConfig._settingFontCenterTextColor);
        fontText.setBackgroundColor(ThemeConfig._baseBackgroundColor);
        fontText.setText(mList[position]);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return mList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
