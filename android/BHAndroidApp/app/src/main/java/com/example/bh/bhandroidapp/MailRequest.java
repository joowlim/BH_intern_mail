package com.example.bh.bhandroidapp;

import android.util.JsonReader;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.spec.ECField;

/**
 * Created by BH on 2017-07-11.
 */

public class MailRequest {
    URL requestURL;
    HttpURLConnection connection;

    public MailRequest(String url){
        try{
            requestURL = new URL(url);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public MailEntry requestToServer(){
        new Thread(){
            public void run(){

                try{
                    connection = (HttpURLConnection) requestURL.openConnection();
                }catch(Exception e){
                    Log.e("requestToServer()","openConnection error");
                    e.printStackTrace();
                }

                try{
                    if(connection.getResponseCode() != 200){
                        connection.disconnect();
                        return;
                    }
                }catch(Exception e){
                    Log.e("requestToServer()","getResponseCode");
                    e.printStackTrace();
                }

                InputStream responseBody=null;
                InputStreamReader responseBodyReader=null;
                JsonReader jsonReader=null;

                try{
                    responseBody = connection.getInputStream();
                    responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");
                    jsonReader = new JsonReader(responseBodyReader);
                }catch(Exception e){
                    Log.e("requestToServer()","assign response, reader err");
                    e.printStackTrace();
                }

                try{
                    jsonReader.beginObject();
                    while(jsonReader.hasNext()){
                        String key = jsonReader.nextName();
                        String value = jsonReader.nextString();
                        Log.i("result",key + " : " + value);
                    }
                    jsonReader.close();
                    connection.disconnect();
                }catch(Exception e){
                    Log.e("requestToServer()","print json result err");
                    e.printStackTrace();
                }
            }
        }.start();

        return new MailEntry(1, "title","sender","receiver","date","body");
    }
}
