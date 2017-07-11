package com.example.bh.bhandroidapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by BH on 2017-07-11.
 */

public class MailRequest {
    private URL requestURL;
    private HttpURLConnection connection;
    private Context context = null;
    private RequestAsyncTask asyncTask;

    public MailRequest(String url,Context c){
        try{
            requestURL = new URL(url);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        context = c;
    }

    public void requestToServer(){
        asyncTask = new RequestAsyncTask();
        asyncTask.execute();
    }

    protected class RequestAsyncTask extends AsyncTask<String,String,MailEntry>{

        @Override
        protected void onPostExecute(final MailEntry mailEntry) {
            super.onPostExecute(mailEntry);
            ((ActivityMain)context).getMailListAdapter().addItem(mailEntry);
            ((ActivityMain)context).getMailListAdapter().notifyDataSetChanged();
        }

        @Override
        protected MailEntry doInBackground(String... params) {
            try{
                connection = (HttpURLConnection) requestURL.openConnection();
            }catch(Exception e){
                Log.e("requestToServer()","openConnection error");
                e.printStackTrace();
            }

            try{
                if(connection.getResponseCode() != 200){
                    connection.disconnect();
                    return null;
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

            return new MailEntry(1,"a","b","c","d","e");
        }
    }

}
