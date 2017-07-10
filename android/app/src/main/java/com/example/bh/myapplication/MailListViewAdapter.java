package com.example.bh.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by BH on 2017-07-10.
 */

public class MailListViewAdapter extends BaseAdapter {
    private ArrayList<MailEntry> listViewItemList;


    public MailListViewAdapter(){
        listViewItemList = new ArrayList<MailEntry>();
    }

    @Override
    public int getCount(){
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_mails,parent,false);

        }
        TextView subject = (TextView) convertView.findViewById(R.id.subject);
        TextView sender = (TextView) convertView.findViewById(R.id.sender);
        TextView receiver = (TextView) convertView.findViewById(R.id.receiver);
        TextView date = (TextView) convertView.findViewById(R.id.date);

        MailEntry item = listViewItemList.get(position);

        subject.setText(item.getSubject());
        sender.setText(item.getSender());
        receiver.setText(item.getReceiver());
        date.setText(item.getDate());

        return convertView;
    }

    @Override
    public long getItemId(int pos){
        return pos;
    }

    @Override
    public Object getItem(int pos){
        return listViewItemList.get(pos);
    }
    public void addItem(MailEntry entry){
        listViewItemList.add(entry);
    }
}
