package com.example.bh.bhandroid;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Date;

public class ActivityMain extends AppCompatActivity {

    MailEntry test1, test2;
    ListView listview;
    MailListViewAdapter adapter;

    private EditText editSearchBySubject;
    private EditText editSearchBySender;
    private EditText editSearchByReceiver;
    private EditText editSearchByKeyword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test1 = new MailEntry(1, "Subject 1","sender 1", "receiver 1","2017/07/10","text 1");
        test2 = new MailEntry(2, "Subject 2","sender 2", "receiver 2","2017/01/11","text 2");

        initVariables();


        adapter.addItem(test1);
        adapter.addItem(test2);

    }
    public void initVariables(){
        //리스트뷰 및 어댑터 setting
        adapter = new MailListViewAdapter();
        listview = (ListView) findViewById(R.id.listview_mail_list);
        listview.setAdapter(adapter);

        initView();
    }
    public void initView(){
        editSearchBySubject = (EditText)findViewById(R.id.edit_search_by_subject);
        editSearchBySender = (EditText)findViewById(R.id.edit_search_by_sender);
        editSearchByReceiver = (EditText) findViewById(R.id.edit_search_by_receiver);
        editSearchByKeyword = (EditText) findViewById(R.id.edit_search_by_keyword);
    }
    public void onClickBtnGetREST(View v){
        Date date = new Date();
        adapter.addItem(new MailEntry(adapter.getCount() + 1,"Subject" +(adapter.getCount() + 1),"sender","receiver",date.toString(),"body"));
        adapter.notifyDataSetChanged();
    }
}
