package com.example.bh.bhandroidapp;


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
    private ArrayList<MailEntry> listViewItemListOriginal;

    public MailListViewAdapter(){
        listViewItemList = new ArrayList<MailEntry>();
        listViewItemListOriginal = new ArrayList<MailEntry>();
    }

    @Override
    public int getCount(){
        return listViewItemList.size();
    }
    public int getTotalCount(){
        return listViewItemListOriginal.size();
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
        TextView receiver = (TextView) convertView.findViewById(R.id.real_receiver);
        //TextView refReceiver = (TextView) convertView.findViewById(R.id.ref_receiver);
        TextView date = (TextView) convertView.findViewById(R.id.date);

        MailEntry item = listViewItemList.get(position);

        subject.setText(item.getSubject());
        sender.setText(item.getSender());
        receiver.setText(item.getRealReceiver());
        //refReceiver.setText(item.getRefReceiver());
        date.setText(item.getDate());

        return convertView;
    }
    public void clearViewItem(){
        listViewItemList.clear();
        //listViewItemList = (ArrayList<MailEntry>)listViewItemListOriginal.clone();
    }
    public void resetItem(){
        listViewItemList.clear();
        listViewItemList = (ArrayList<MailEntry>)listViewItemListOriginal.clone();
    }
    public void clearItemAll(){
        listViewItemList.clear();
        listViewItemListOriginal.clear();
    }
    @Override
    public long getItemId(int pos){
        return pos;
    }

    @Override
    public Object getItem(int pos){
        return listViewItemList.get(pos);
    }
    public Object getItemFromOrigin(int pos) { return listViewItemListOriginal.get(pos);}
    public void addItem(MailEntry entry){
        listViewItemList.add(entry);
    }
    public void addItemToOrigin(MailEntry entry){
        listViewItemListOriginal.add(entry);
    }
    public void addItemToAll(MailEntry entry){
        addItem(entry);
        addItemToOrigin(entry);
    }
}
