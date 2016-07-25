package com.dudu.aios.ui.map.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dudu.android.launcher.R;
import com.dudu.navi.vauleObject.NaviDriveMode;

import java.util.ArrayList;

/**
 * Created by lxh on 2016/2/15.
 */
public class RouteStrategyAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<NaviDriveMode> data;

    private LayoutInflater inflater;

    public RouteStrategyAdapter(Context context, ArrayList<NaviDriveMode> data) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.drive_strategy_item, parent, false);
            holder.tvDriveStrategy = (TextView) convertView.findViewById(R.id.text_drive_strategy);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvDriveStrategy.setText((position + 1) + "." + data.get(position).getName());
        return convertView;
    }

    class ViewHolder {
        TextView tvDriveStrategy;
    }


}
