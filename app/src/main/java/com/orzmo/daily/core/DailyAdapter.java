package com.orzmo.daily.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orzmo.daily.R;

import java.util.List;

public class DailyAdapter extends ArrayAdapter {
    private int resourceId;
    public DailyAdapter(Context context, int resource, List<Daily> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public void removeItem(int position, List<Daily> dailies){
        dailies.remove(position);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Daily daily = (Daily) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView datetime = (TextView) view.findViewById(R.id.datetime);
        title.setText(daily.getTitle());
        datetime.setText(daily.getDatetime());
        username.setText(daily.getUsername());
        return view;
    }


}
