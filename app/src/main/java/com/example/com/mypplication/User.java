package com.example.com.mypplication;

import android.app.Activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/12.
 */
/**********用户类**********/
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public String username=new String(""),
            password=new String(""),
            invitor=new String(""),
            invitecode=new String(""),
            sessionid=new String("");
    public Note note=new Note();
    public List<Note> passnotelist=new ArrayList<Note>();
    public List<String>classify;
    public String searchkeyword;
    public int operation;
    public String copyvalue=new String("");
    public Activity activity;
    public Integer mainflag=0;

    public Integer getMainflag() {
        return mainflag;
    }

    public void setMainflag(Integer mainflag) {
        this.mainflag = mainflag;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public String getCopyvalue() {
        return copyvalue;
    }

    public void setCopyvalue(String copyvalue) {
        this.copyvalue = copyvalue;
    }

    public String getSearchkeyword() {
        return searchkeyword;
    }

    public void setSearchkeyword(String searchkeyword) {
        this.searchkeyword = searchkeyword;
    }

    public List<String> getClassify() {
        return classify;
    }

    public List<Note> getPassnotelist() {
        return passnotelist;
    }

    public void setPassnotelist(List<Note> passnotelist) {
        this.passnotelist = passnotelist;
    }

    public void setClassify(List<String> classify) {
        this.classify = classify;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public static long getSerialVersionUID() {return serialVersionUID;}

    public Note getpassNote() {return note;}

    public void setpassNote(Note note) {this.note = note;}

    public User() {}

    public User(String username,String password,String invitor,String invitecode) {this.username=username;this.password=password;this.invitor=invitor;this.invitecode=invitecode;}

    public  User(String username,String password) {this.username=username;this.password=password;}

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setInvitor(String invitor) {
        this.invitor = invitor;
    }

    public void setInvitecode(String invitecode) {
        this.invitecode = invitecode;
    }

    public String getInvitecode() {
        return invitecode;
    }

    public String getInvitor() {
        return invitor;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {return username;}

    public void setSessionid(String sessionid)
    {
        this.sessionid=sessionid;
    }

    public String getSessionid()
    {
        return this.sessionid;
    }
    public void clear()
    {
        this.setClassify(null);
        this.setNote(null);
        this.setNote(new Note());
        this.setSessionid("");
        this.setInvitor("");
        this.setInvitecode("");
        this.setUsername("");
    }
}
