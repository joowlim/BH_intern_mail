package com.example.bh.bhandroidapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ActivitySettings extends AppCompatActivity {

    EditText editServerIP;
    EditText editMailId;
    EditText editMailHost;
    SharedPreferences prefs ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initVar();


    }
    public boolean isFirstStart(){
        boolean firstStart = prefs.getBoolean("firstStart",true);
        return firstStart;
    }
    public void initVar(){
        editServerIP = (EditText)findViewById(R.id.edit_server_ip);
        editMailId = (EditText)findViewById(R.id.edit_mail_id);
        editMailHost = (EditText)findViewById(R.id.edit_mail_host);
        prefs = getSharedPreferences("settings",MODE_PRIVATE);
        editServerIP.setText(prefs.getString("serverIP",""));
        editMailId.setText(prefs.getString("mailId",""));
        editMailHost.setText(prefs.getString("mailHost",""));
    }
    public void onClickBtnConfirmSettings(View v){
        String serverIP = editServerIP.getText().toString();
        String mailId = editMailId.getText().toString();
        String mailHost = editMailHost.getText().toString();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.putString("serverIP",serverIP);
        editor.putString("mailId",mailId);
        editor.putString("mailHost",mailHost);
        editor.commit();
        finish();
    }
    public void onClickBtnCancelSettings(View v){
        finish();
    }

}
