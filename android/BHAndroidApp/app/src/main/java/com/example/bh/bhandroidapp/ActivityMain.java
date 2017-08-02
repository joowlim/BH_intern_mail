package com.example.bh.bhandroidapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;


public class ActivityMain extends AppCompatActivity {

    private ListView listview;
    private MailListViewAdapter adapter;
    private Resources res;
    private EditText editSearchBySubject;
    private EditText editSearchBySender;
    private EditText editSearchByReceiver;
    private EditText editSearchByKeyword;
    private SharedPreferences prefs ;
    private LinearLayout linearFilterContainer;
    private MailRequest mailRequest ;
    private boolean isFilterOn = true;
    private Button btnFilterToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isFirstStart() == true){ //최초 실행시
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.first_start_must_do_settings), Toast.LENGTH_SHORT).show();
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
        res = getResources();

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

                String message = String.format(res.getString(R.string.mail_detail_info_format),
                        sender,realReceiver,refReceiver==null ? res.getString(R.string.nothing) : refReceiver ,date,innerText);

                AlertDialog.Builder mailDialog = new AlertDialog.Builder(ActivityMain.this);
                mailDialog.setTitle(subject);
                mailDialog.setMessage(message);
                mailDialog.setPositiveButton(res.getString(R.string.confirm),null);
                mailDialog.show();

            }
        });
        initView();

        if(isFirstStart() == false){
            String serverIP = prefs.getString("serverIP","");
            String mailList = prefs.getString("mailList","{\"mail_list\":[]}");
            JSONArray tempMailList = null;

            try{
                tempMailList = new JSONObject(mailList).getJSONArray("mail_list");

            }catch(Exception e){
                e.printStackTrace();
            }
            StringBuffer requestParams = new StringBuffer("?");

            if(tempMailList == null){
                return ;
            }

            for(int i = 0;i<tempMailList.length();i++){
                try{
                    requestParams.append(String.format("mail_list[]=%s&",tempMailList.getString(i)));
                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.e("err idx",i+"");
                }
            }

            mailRequest = new MailRequest("http://"+serverIP+"/api.php/"+requestParams.toString(),this);
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

        linearFilterContainer = (LinearLayout)findViewById(R.id.linear_filter_container);
        btnFilterToggle = (Button)findViewById(R.id.btn_toggle_filter);
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
        String mailList = prefs.getString("mailList","{\"mail_list\":[]}");
        JSONArray tempMailList = null;

        try{

            tempMailList = new JSONObject(mailList).getJSONArray("mail_list");

        }catch(Exception e){
            e.printStackTrace();
        }
        StringBuffer requestParams = new StringBuffer("?");

        if(tempMailList == null){
            return ;
        }

        for(int i = 0;i<tempMailList.length();i++){
            try{
                requestParams.append(String.format("mail_list[]=%s&",tempMailList.getString(i)));
            }
            catch (Exception e){
                e.printStackTrace();
                Log.e("err idx",i+"");
            }
        }

        mailRequest = new MailRequest("http://"+serverIP+"/api.php/"+requestParams.toString(),this);
        mailRequest.requestToServer();
    }
    public void onClickBtnResetFilter(View v){
        editSearchBySubject.setText("");
        editSearchBySender.setText("");
        editSearchByReceiver.setText("");
        editSearchByKeyword.setText("");
        searchItemWithFilter();
    }
    public void onClickBtnToggleFilter(View v){
        if(isFilterOn){
            btnFilterToggle.setText(res.getString(R.string.btn_toggle_filter_to_on));
            linearFilterContainer.setVisibility(View.GONE);
        }
        else{
            btnFilterToggle.setText(res.getString(R.string.btn_toggle_filter_to_off));
            linearFilterContainer.setVisibility(View.VISIBLE);
        }
        isFilterOn = !isFilterOn;
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
