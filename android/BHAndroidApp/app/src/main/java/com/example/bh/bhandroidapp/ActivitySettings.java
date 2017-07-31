package com.example.bh.bhandroidapp;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class ActivitySettings extends AppCompatActivity {

    private EditText editServerIP;
    private EditText editMailId;
    private EditText editMailHost;
    private SharedPreferences prefs ;
    private Resources res;

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
        res = getResources();
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

        if(isIPFormat(serverIP) == false){
            Toast.makeText(getApplicationContext(),res.getString(R.string.not_valid_IP_format), Toast.LENGTH_SHORT).show();
            return ;

        }

        String mailId = editMailId.getText().toString();
        String mailHost = editMailHost.getText().toString();


        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.putString("serverIP",serverIP);
        editor.putString("mailId",mailId);
        editor.putString("mailHost",mailHost);
        editor.commit();
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.confirm_settings), Toast.LENGTH_SHORT).show();
        finish();
    }
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean isIPFormat(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }
    public void onClickBtnCancelSettings(View v){
        if(isFirstStart() == true){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.first_start_must_do_settings), Toast.LENGTH_SHORT).show();
            return;
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(isFirstStart() == true) return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
