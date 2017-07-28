package com.example.bh.bhandroidapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Date;


public class ActivityMain extends AppCompatActivity {

    private ListView listview;
    private MailListViewAdapter adapter;

    private EditText editSearchBySubject;
    private EditText editSearchBySender;
    private EditText editSearchByReceiver;
    private EditText editSearchByKeyword;
    private SharedPreferences prefs ;

    private MailRequest mailRequest ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isFirstStart() == true){ //최초 실행시
            Toast.makeText(getApplicationContext(),"최초 실행시 서버 ip와 계정을 설정해야 합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ActivityMain.this,ActivitySettings.class);
            startActivity(intent);
        }
        initVariables();
    }
    public boolean isFirstStart(){
        prefs = getSharedPreferences("settings",MODE_PRIVATE);
        boolean isFirst = prefs.getBoolean("firstStart",true);
        return isFirst;
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
                String realReceiver = clickedMailEntry.getRealReceiver();
                String refReceiver = clickedMailEntry.getRefReceiver();
                String date = clickedMailEntry.getDate();
                String innerText = clickedMailEntry.getInnerText();

                String message = String.format("보낸이 : %s\n받는이 : %s\n참조 : %s\n날짜 : %s\n\n본문\n%s",
                        sender,realReceiver,refReceiver==null ? "없음" : refReceiver ,date,innerText);

                AlertDialog.Builder mailDialog = new AlertDialog.Builder(ActivityMain.this);
                mailDialog.setTitle(subject);
                mailDialog.setMessage(message);
                mailDialog.setPositiveButton("확인",null);
                mailDialog.show();

            }
        });

        initView();

        if(isFirstStart() == false){
            String serverIP = prefs.getString("serverIP","");
            String mailId = prefs.getString("mailId","");
            String mailHost = prefs.getString("mailHost","");
            mailRequest = new MailRequest("http://"+serverIP+"/api.php/"+mailId+"@"+mailHost,this);
            mailRequest.requestToServer();
        }




    }
    public void initView(){
        editSearchBySubject = (EditText)findViewById(R.id.edit_search_by_subject);
        editSearchBySubject.addTextChangedListener(filterTextChangerListener);

        editSearchBySender = (EditText)findViewById(R.id.edit_search_by_sender);
        editSearchBySender.addTextChangedListener(filterTextChangerListener);

        editSearchByReceiver = (EditText) findViewById(R.id.edit_search_by_receiver);
        editSearchByReceiver.addTextChangedListener(filterTextChangerListener);

        editSearchByKeyword = (EditText) findViewById(R.id.edit_search_by_keyword);
        editSearchByKeyword.addTextChangedListener(filterTextChangerListener);

    }
    TextWatcher filterTextChangerListener = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchItemWithFilter();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    
    public void searchItemWithFilter(){
        Date date = new Date();
        String subject = editSearchBySubject.getText().toString();
        String sender = editSearchBySender.getText().toString();
        String receiver = editSearchByReceiver.getText().toString();
        String keyword = editSearchByKeyword.getText().toString();

        if(subject.equals("") && sender.equals("") && receiver.equals("") && keyword.equals("")) {
            adapter.resetItem();
            adapter.notifyDataSetChanged();
            return;
        }
        adapter.clearViewItem();

        for(int i = 0 ;i<adapter.getTotalCount();i++){
            if(isMatch(subject,sender,receiver,keyword,(MailEntry)adapter.getItemFromOrigin(i))){
                adapter.addItem((MailEntry)adapter.getItemFromOrigin(i));
            }
        }
        adapter.notifyDataSetChanged();
    }
    public boolean isMatch(String subject,String sender,String receiver,String keyword,MailEntry entry){
        String entrySubject = entry.getSubject();
        String entrySender = entry.getSender();
        String entryReceiver = entry.getRealReceiver();
        String entryInnerText = entry.getInnerText();

        if(!entrySubject.contains(subject)){
            return false;
        }
        if(!entrySender.contains(sender)){
            return false;
        }
        if(!entryReceiver.contains(receiver)){
            return false;
        }
        if(!entryInnerText.contains(keyword)){
            return false;
        }
        return true;
    }
    public void onClickBtnGetREST(View v){
        String serverIP = prefs.getString("serverIP","");
        String mailId = prefs.getString("mailId","");
        String mailHost = prefs.getString("mailHost","");
        Log.i("serverIP",serverIP);
        Log.i("maild",mailId);
        Log.i("mailHost",mailHost);
        mailRequest = new MailRequest("http://"+serverIP+"/api.php/"+mailId+"@"+mailHost,this);
        mailRequest.requestToServer();
    }
    public void onClickBtnResetFilter(View v){
        editSearchBySubject.setText("");
        editSearchBySender.setText("");
        editSearchByReceiver.setText("");
        editSearchByKeyword.setText("");
        searchItemWithFilter();
    }
    public MailListViewAdapter getMailListAdapter(){
        return adapter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_settings){
            Intent intent = new Intent(ActivityMain.this, ActivitySettings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
