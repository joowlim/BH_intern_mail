package com.example.bh.bhandroidapp;


/**
 * Created by BH on 2017-07-10.
 */

public class MailEntry
{
    private int mailId;
    private String subject;
    private String sender;
    private String realReceiver;
    private String refReceiver;
    private String date;
    private boolean existRef =false;
    private String innerText;
    public MailEntry(int id, String sub,String sen, String realReceiver,String refReceiver, String date,String text){
        mailId = id;
        subject = sub;
        sender = sen;
        this.realReceiver = realReceiver;
        this.refReceiver = refReceiver;
        this.date = date;
        innerText = text;
    }
    public MailEntry(int id, String subject,String sender, String date, String text){
        mailId = id;
        this.subject = subject;
        this.sender = sender;
        this.date = date;
        this.innerText = text;

    }
    public boolean getExistRef(){
        return existRef;
    }
    public void setExistRef(boolean existRef){
        this.existRef = existRef;
    }
    public int getMailId(){
        return mailId;
    }

    public void setMailId(int mailId) {
        this.mailId = mailId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }


    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRealReceiver() {
        return realReceiver;
    }

    public void setRealReceiver(String receiver) {
        this.realReceiver = receiver;
    }

    public String getRefReceiver(){
        return refReceiver;
    }
    public void setRefReceiver(String refReceiver){
        this.refReceiver = refReceiver;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInnerText() {
        return innerText;
    }

    public void setInnerText(String innerText) {
        this.innerText = innerText;
    }
}
