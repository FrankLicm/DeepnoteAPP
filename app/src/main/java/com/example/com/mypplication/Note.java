package com.example.com.mypplication;

import android.content.Intent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/*********笔记类**********/
public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    String noteid;//笔记id
    String owerid;//作者id
    String owner;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    String title;//标题
    String time;//提交时间
    String auth;//私有权限(1为私密0为公开)
    String retrash;//是否处于回收站状态(0为否1为是)
    String prilabel;//主标签
    String praisenum;//点赞人数
    String commentnum;//评论人数
    String source;//出处(某本书or文章之类)
    String sourcewriter;//原文的作者
    String link=new String("");//可选原文链接
    String ref=new String("");//引用的原文
    String feel;//自己的评注
    String []labels={};//副标签组[]
    Integer classify;
    Integer popularity;

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    List<Comment> comments=new ArrayList<Comment>();

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Integer getClassify() {
        return classify;
    }

    public void setClassify(Integer classify) {
        this.classify = classify;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getOwerid() {
        return owerid;
    }

    public void setOwerid(String owerid) {
        this.owerid = owerid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getRetrash() {
        return retrash;
    }

    public void setRetrash(String retrash) {
        this.retrash = retrash;
    }

    public String getPrilabel() {
        return prilabel;
    }

    public void setPrilabel(String prilabel) {
        this.prilabel = prilabel;
    }

    public String getPraisenum() {
        return praisenum;
    }

    public void setPraisenum(String praisenum) {
        this.praisenum = praisenum;
    }

    public String getCommentnum() {
        return commentnum;
    }

    public void setCommentnum(String commentnum) {
        this.commentnum = commentnum;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourcewriter() {
        return sourcewriter;
    }

    public void setSourcewriter(String sourcewriter) {
        this.sourcewriter = sourcewriter;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getFeel() {
        return feel;
    }

    public void setFeel(String feel) {
        this.feel = feel;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }
}
/***评论类****/
class Comment implements Serializable
{
    private static final long serialVersionUID = 1L;
    String username;
    String time;
    String content;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
/******笔记的种类类********/
class Kind implements Serializable
{
    private static final long serialVersionUID = 1L;
    String kindid;
    String name;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getKindid() {
        return kindid;
    }

    public void setKindid(String kindid) {
        this.kindid = kindid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}