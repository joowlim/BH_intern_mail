package com.example.bh.bhandroidapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Stack;
import java.util.regex.Pattern;

public class ActivitySettings extends AppCompatActivity {

    private EditText editServerIP;
    private SharedPreferences prefs ;
    private Resources res;
    private Stack<FragmentEntryMailSetting> currentEntry = new Stack<FragmentEntryMailSetting>();
    private String savedMailSet;
    private FragmentManager fragmentManager;
    private Gson gson = new Gson();
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
        fragmentManager = getFragmentManager();
        editServerIP = (EditText)findViewById(R.id.edit_server_ip);

        prefs = getSharedPreferences("settings",MODE_PRIVATE);
        editServerIP.setText(prefs.getString("serverIP",""));
        savedMailSet = prefs.getString("mailList","{\"mail_list\":[]}");
        JSONArray tempSavedMailSet = null;
        try{
            tempSavedMailSet = new JSONObject(savedMailSet).getJSONArray("mail_list");
        }
        catch(Exception e){
            e.printStackTrace();
        }


        if(tempSavedMailSet == null){
            return ;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for(int i = 0 ;i<tempSavedMailSet.length();i++){
            try{
                FragmentEntryMailSetting temp = FragmentEntryMailSetting.newInstance(tempSavedMailSet.getString(i));
                fragmentTransaction.add(R.id.linear_mail_list_container, temp, "");
                currentEntry.push(temp);
            }
            catch(Exception e){
                e.printStackTrace();
                Log.e("err idx",i + "");
                Log.e("err string",tempSavedMailSet.toString());
            }
        }
        fragmentTransaction.commit();
    }

    public void onClickBtnConfirmSettings(View v){

        if(currentEntry.size() == 0){
            Toast.makeText(getApplicationContext(),R.string.no_mail_entry,Toast.LENGTH_SHORT).show();
            return;
        }
        String serverIP = editServerIP.getText().toString();
        if(isIPFormat(serverIP) == false){
            Toast.makeText(getApplicationContext(),res.getString(R.string.not_valid_IP_format), Toast.LENGTH_SHORT).show();
            return ;
        }
        Object[] tempCurrentEntryArray = currentEntry.toArray();
        for(int i = 0 ;i<tempCurrentEntryArray.length;i++){
            if(((FragmentEntryMailSetting)tempCurrentEntryArray[i]).getMailHostToString().equals("") ||((FragmentEntryMailSetting)tempCurrentEntryArray[i]).getMailIdToString().equals("")){
                Toast.makeText(getApplicationContext(),res.getString(R.string.there_is_black), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String currentEntryString = "]}";
        if(currentEntry.empty() == false){
            currentEntryString ="\""+currentEntry.pop().getFullMailEntry() +"\""+ currentEntryString;

        }
        while(currentEntry.empty() == false){

            currentEntryString ="\""+currentEntry.pop().getFullMailEntry() +"\", "+ currentEntryString;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.putString("serverIP",serverIP);
        editor.remove("mailList");
        editor.putString("mailList","{\"mail_list\":["+currentEntryString);
        editor.commit();
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.confirm_settings), Toast.LENGTH_SHORT).show();
        finish();
    }
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean isIPFormat(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(isFirstStart() == true){
                Toast.makeText(getApplicationContext(),res.getString(R.string.first_start_must_do_settings), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onClickBtnAddMail(View v){
        if(currentEntry.size() == 10){
            Toast.makeText(getApplicationContext(),res.getString(R.string.cannot_push_more_than_10),Toast.LENGTH_SHORT).show();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentEntryMailSetting temp = new FragmentEntryMailSetting();
        fragmentTransaction.add(R.id.linear_mail_list_container, temp, "");
        fragmentTransaction.commit();
        currentEntry.push(temp);
    }
    public void onClickBtnRemoveMail(View v){
        if(currentEntry.size() == 0){
            return ;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(currentEntry.pop());
        fragmentTransaction.commit();
    }
}
