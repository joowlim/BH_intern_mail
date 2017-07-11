package com.example.bh.bhandroidapp;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import java.util.Date;


public class ActivityMain extends AppCompatActivity {

    private ListView listview;
    private MailListViewAdapter adapter;

    private EditText editSearchBySubject;
    private EditText editSearchBySender;
    private EditText editSearchByReceiver;
    private EditText editSearchByKeyword;

    private MailRequest mailRequest = new MailRequest("http://echo.jsontest.com/key/value/one/two",this);;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariables();
    }
    public void initVariables(){
        //리스트뷰 및 어댑터 setting
        adapter = new MailListViewAdapter();
        listview = (ListView) findViewById(R.id.listview_mail_list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MailEntry clickedMailEntry = (MailEntry)adapter.getItem(position);

                String subject = clickedMailEntry.getSubject();
                String sender = clickedMailEntry.getSender();
                String receiver = clickedMailEntry.getReceiver();
                String date = clickedMailEntry.getDate();
                String innerText = clickedMailEntry.getInnerText();

                String message = String.format("보낸이 : %s\n받는이 : %s\n날짜 : %s\n\n본문\n%s",
                        sender,receiver,date,innerText);

                AlertDialog.Builder mailDialog = new AlertDialog.Builder(ActivityMain.this);
                mailDialog.setTitle(subject);
                mailDialog.setMessage(message);
                mailDialog.setPositiveButton("확인",null);
                mailDialog.show();

            }
        });

        initView();
    }
    public void initView(){
        editSearchBySubject = (EditText)findViewById(R.id.edit_search_by_subject);
        editSearchBySender = (EditText)findViewById(R.id.edit_search_by_sender);
        editSearchByReceiver = (EditText) findViewById(R.id.edit_search_by_receiver);
        editSearchByKeyword = (EditText) findViewById(R.id.edit_search_by_keyword);
    }
    public void onClickBtnInsertData(View v){
        Date date = new Date();
        String subject = editSearchBySubject.getText().toString();
        String sender = editSearchBySender.getText().toString();
        String receiver = editSearchByReceiver.getText().toString();
        String keyword = editSearchByKeyword.getText().toString();
        adapter.addItem(new MailEntry(adapter.getCount() + 1,subject,sender,receiver,date.toString(),keyword));
        adapter.notifyDataSetChanged();
    }
    public void onClickBtnGetREST(View v){
        mailRequest.requestToServer();
    }

    public MailListViewAdapter getMailListAdapter(){
        return adapter;
    }
}
