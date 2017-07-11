package com.example.bh.bhandroidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;

public class ActivityMain extends AppCompatActivity {

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

        initVariables();
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
    public void onClickBtnInsertData(View v){
        Date date = new Date();
        String subject = editSearchBySubject.getText().toString();
        String sender = editSearchBySender.getText().toString();
        String receiver = editSearchByReceiver.getText().toString();
        adapter.addItem(new MailEntry(adapter.getCount() + 1,subject,sender,receiver,date.toString(),"body"));
        adapter.notifyDataSetChanged();


    }
    public void onClickBtnGetREST(View v){
        new Thread(){
            public void run(){
// Create URL
                try{
                    URL requestURL = new URL("http://echo.jsontest.com/key/value/one/two");

// Create connection
                    HttpURLConnection myConnection =
                            (HttpURLConnection) requestURL.openConnection();

                    /*
                    myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                    myConnection.setRequestProperty("Accept",
                            "application/vnd.github.v3+json");
                    myConnection.setRequestProperty("Contact-Me",
                            "hathibelagal@example.com");*/

                    if (myConnection.getResponseCode() == 200) {

                        InputStream responseBody = myConnection.getInputStream();

                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");

                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) { // Loop through all keys
                            String key = jsonReader.nextName(); // Fetch the next key
                            Log.i("result",key + " : " +jsonReader.nextString());
                        }

                        jsonReader.close();
                    } else {
                        Log.e("error","responseCode is not 200");
                    }
                    myConnection.disconnect();
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }.start();

    }
}
