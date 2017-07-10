package com.example.bh.bhandroid;


/**
 * Created by BH on 2017-07-10.
 */

public class MailEntry
{
    private int mailId;
    private String subject;
    private String sender;
    private String receiver;
    private String date;
    private String innerText;
    public MailEntry(int id, String sub,String sen, String rec, String date,String text){
        mailId = id;
        subject = sub;
        sender = sen;
        receiver = rec;
        this.date = date;
        innerText = text;
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
