package com.orzmo.Memo.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orzmo.Memo.R;

import java.util.List;

public class MemoAdapter extends ArrayAdapter {
    private int resourceId;
    public MemoAdapter(Context context, int resource, List<Memo> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public void removeItem(int position, List<Memo> dailies){
        dailies.remove(position);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Memo memo = (Memo) getItem(position);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView datetime = (TextView) view.findViewById(R.id.datetime);
        title.setText(memo.getTitle());
        datetime.setText(memo.getDatetime());
        username.setText(memo.getUsername());
        return view;
    }


}
