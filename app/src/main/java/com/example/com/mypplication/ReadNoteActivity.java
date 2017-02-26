package com.example.com.mypplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/*********读笔记********/
public  class ReadNoteActivity extends AppCompatActivity {
   User user=new User();
    Note note=new Note();
    List<Comment> comments;
    ListView commentlist;
    SimpleAdapter simpleAdapter;
    List<HashMap<String, String>> mlist;
   public static String SER_KEY= "com.example.com.mypplication";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user=(User)getIntent().getSerializableExtra(SER_KEY);
        note=user.getpassNote();
        comments=note.getComments();
        setContentView(R.layout.activity_read_note);
        Log.d("usersession",user.getSessionid());
        //Log.d("noteowner",note.getOwnerid());
        ImageButton praiseButton= (ImageButton)findViewById(R.id.praiseButton);
        ImageButton collectButton=(ImageButton)findViewById(R.id.collectButton);
        ImageButton commentButton=(ImageButton)findViewById(R.id.commentButton);
        TextView title=(TextView)findViewById(R.id.notetitle);
        title.setText(note.getTitle());
       final TextView praisenum=(TextView)findViewById(R.id.praisenum);
        praisenum.setText("点赞数:"+note.getPraisenum());
        final TextView commentnum=(TextView)findViewById(R.id.commentnum);
        commentnum.setText("评论数:"+note.getCommentnum());
        TextView writer=(TextView)findViewById(R.id.writer);
        writer.setText(note.getOwner());
        TextView prilabel=(TextView)findViewById(R.id.prilabel);
        prilabel.setText(note.getPrilabel());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String time = sdf.format(new Date(Long.parseLong(note.getTime())*1000));
        TextView notetime=(TextView)findViewById(R.id.notetime);
        notetime.setText(time);
        TextView notefeel=(TextView)findViewById(R.id.notefeel);
        notefeel.setText(note.getFeel());
        TextView noterefer=(TextView)findViewById(R.id.noterefer);
        noterefer.setText(note.getRef());
        praiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("praseclick", "click");
                praiseRunnable r = new praiseRunnable();
                Thread t = new Thread(r);
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (r.result.equals("true")) {
                    new AlertDialog.Builder(ReadNoteActivity.this)
                            .setMessage("点赞成功")
                            .setPositiveButton("确定", null)
                            .show();
                    note.setPraisenum("" + (Integer.valueOf(note.getPraisenum()) + 1));
                    praisenum.setText("点赞数:" + note.getPraisenum());

                } else if (r.result.equals("false")) {
                    new AlertDialog.Builder(ReadNoteActivity.this)
                            .setMessage("点赞失败:" + r.feedback)
                            .setPositiveButton("确定", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ReadNoteActivity.this)
                            .setMessage("点赞失败:错误未知")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectRunnable r = new collectRunnable();
                Thread t = new Thread(r);
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (r.result.equals("true")) {
                    new AlertDialog.Builder(ReadNoteActivity.this)
                            .setMessage("收藏成功")
                            .setPositiveButton("确定", null)
                            .show();
                } else if (r.result.equals("false")) {
                    new AlertDialog.Builder(ReadNoteActivity.this)
                            .setMessage("收藏失败:" + r.feedback)
                            .setPositiveButton("确定", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ReadNoteActivity.this)
                            .setMessage("收藏失败:错误未知")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(ReadNoteActivity.this);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(ReadNoteActivity.this);
                builder2.setTitle("评论");
                builder2.setMessage("请输入评论内容:");
                builder2.setView(edittext);
                builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!edittext.getText().toString().equals("")) {
                            commentRunnable r = new commentRunnable();
                            r.setContent(edittext.getText().toString());
                            Thread t = new Thread(r);
                            t.start();
                            try {
                                t.join();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (r.result.equals("true")) {
                                new AlertDialog.Builder(ReadNoteActivity.this)
                                        .setMessage("评论成功")
                                        .setPositiveButton("确定", null)
                                        .show();
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put("content",edittext.getText().toString());
                                    map.put("username",user.getUsername());
                                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                                    String time = sdf.format(new Date(System.currentTimeMillis()));
                                    map.put("time", time);
                                    mlist.add(map);
                                    simpleAdapter.notifyDataSetChanged();
                                note.setCommentnum("" + (Integer.valueOf(note.getCommentnum()) + 1));
                                commentnum.setText("评论数:" + note.getCommentnum());
                            } else if (r.result.equals("false")) {
                                new AlertDialog.Builder(ReadNoteActivity.this)
                                        .setMessage("评论失败:" + r.feedback)
                                        .setPositiveButton("确定", null)
                                        .show();
                            } else {
                                new AlertDialog.Builder(ReadNoteActivity.this)
                                        .setMessage("评论失败:错误未知")
                                        .setPositiveButton("确定", null)
                                        .show();
                            }
                        } else {
                            new AlertDialog.Builder(ReadNoteActivity.this)
                                    .setMessage("分类名不得为空")
                                    .setPositiveButton("确定", null)
                                    .show();
                        }
                    }
                });
                builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO 自动生成的方法存根
                        dialog.dismiss();

                    }
                });
                builder2.show();
            }
        });
        //Log.d("comments",new ArrayList<Comment>(note.getComments()).toString());
        setListAdpter(note.getComments());
    }
    public void setListAdpter(final List<Comment> comments) {
        mlist = new ArrayList<HashMap<String, String>>();
        for (Iterator<Comment> iterator = comments.iterator(); iterator.hasNext(); ) {
            Comment comment = (Comment) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("content",comment.getContent());
            map.put("username",comment.getUsername());
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            String time = sdf.format(new Date(Long.parseLong(comment.getTime())*1000));
            map.put("time",time);
            //map.put("url", comments.getUrl());
            mlist.add(map);
        }
        commentlist=(ListView)findViewById(R.id.commentListview);
        simpleAdapter=new SimpleAdapter(this,mlist,R.layout.comment_item,new String[]{"content","username","time"},new int []{R.id.commentcontent,R.id.commentpeople,R.id.commenttime});
        commentlist.setAdapter(simpleAdapter);
    }

    class commentRunnable implements Runnable
    {
        public String content;
        public String result;
        public String feedback;

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public void run() {
            NameValuePair pair1 = new BasicNameValuePair("noteid", note.getNoteid());
            Log.d("noteid",note.getNoteid());
            NameValuePair pair2 = new BasicNameValuePair("ownerid",note.owerid);
            Log.d("ownerid",note.getOwerid());
            NameValuePair pair3 = new BasicNameValuePair("content",content);
            Log.d("content",content);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair1);
            pairList.add(pair2);
            pairList.add(pair3);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList, HTTP.UTF_8);
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/action/CommentNoteController.php");
                request.setEntity(requestHttpEntity);
                String strResult;
                JSONObject jsonObject = null;
                // replace with your url
                    request.setHeader("Cookie", user.getSessionid());

                HttpResponse response;
                try {
                    response = client.execute(request);
                    //Log.d("postresponse",response);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        try {
                            /**读取服务器返回过来的json字符串数据**/
                            strResult = EntityUtils.toString(response.getEntity());
                            jsonObject = getJSON(strResult);
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            /**
                             * jsonObject.getString("code") 取出code
                             * 比如这里返回的json 字符串为 [code:0,msg:"ok",data:[list:{"name":1},{"name":2}]]
                             * **/

                            /**得到data这个key**/
                            result = jsonObject.getString("result");
                            if (result.equals("false")) {
                                feedback = jsonObject.getString("feedback");
                                //Looper.prepare();
                                Log.d("loginfeedback", feedback);
                                //Toast.makeText(LoginActivity.this, feedback, Toast.LENGTH_SHORT).show();
                            } else {

                            }
                            //Toast.makeText(LoginActivity.this, "code:" + jsonObject.getString("code") + "name:" + names, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    } else
                        Toast.makeText(ReadNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong:", "wrong");
                    //e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong2:", "wrong2");
                    e.printStackTrace();
                }
                //Your code goes here
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class collectRunnable implements Runnable
    {
        public String result;
        public String feedback;
        @Override
        public void run() {
            NameValuePair pair = new BasicNameValuePair("noteid", note.getNoteid());
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/action/CollectNoteController.php");
                request.setEntity(requestHttpEntity);
                String strResult;
                JSONObject jsonObject = null;
                // replace with your url
                    request.setHeader("Cookie",user.getSessionid());
                HttpResponse response;
                try {
                    response = client.execute(request);
                    //Log.d("postresponse",response);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        try {
                            /**读取服务器返回过来的json字符串数据**/
                            strResult = EntityUtils.toString(response.getEntity());
                            jsonObject = getJSON(strResult);
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            /**
                             * jsonObject.getString("code") 取出code
                                * 比如这里返回的json 字符串为 [code:0,msg:"ok",data:[list:{"name":1},{"name":2}]]
                                * **/

                                /**得到data这个key**/
                                result = jsonObject.getString("result");
                                if (result.equals("false")) {
                                    feedback = jsonObject.getString("feedback");
                                    //Looper.prepare();
                                    Log.d("loginfeedback", feedback);
                                    //Toast.makeText(LoginActivity.this, feedback, Toast.LENGTH_SHORT).show();
                            } else {

                            }
                            //Toast.makeText(LoginActivity.this, "code:" + jsonObject.getString("code") + "name:" + names, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    } else
                        Toast.makeText(ReadNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong:", "wrong");
                    //e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong2:", "wrong2");
                    e.printStackTrace();
                }
                //Your code goes here
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class praiseRunnable implements Runnable
    {
        public String result;
        public String feedback;
        @Override
        public void run() {
            NameValuePair pair = new BasicNameValuePair("noteid", note.getNoteid());
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/action/PraiseNoteController.php");
                request.setEntity(requestHttpEntity);
                String strResult;
                JSONObject jsonObject = null;
                // replace with your url
                    request.setHeader("Cookie",user.getSessionid());

                HttpResponse response;
                try {
                    response = client.execute(request);
                    //Log.d("postresponse",response);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        try {
                            /**读取服务器返回过来的json字符串数据**/
                            strResult = EntityUtils.toString(response.getEntity());
                            jsonObject = getJSON(strResult);
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            /**
                             * jsonObject.getString("code") 取出code
                             * 比如这里返回的json 字符串为 [code:0,msg:"ok",data:[list:{"name":1},{"name":2}]]
                             * **/

                            /**得到data这个key**/
                            result = jsonObject.getString("result");
                            if (result.equals("false")) {
                                feedback = jsonObject.getString("feedback");
                                //Looper.prepare();
                                Log.d("loginfeedback", feedback);
                                //Toast.makeText(LoginActivity.this, feedback, Toast.LENGTH_SHORT).show();
                            } else {

                            }
                            //Toast.makeText(LoginActivity.this, "code:" + jsonObject.getString("code") + "name:" + names, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    } else
                    {

                    }
                        //Toast.makeText(ReadNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong:", "wrong");
                    //e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong2:", "wrong2");
                    e.printStackTrace();
                }
                //Your code goes here
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public JSONObject getJSON(String sb) throws JSONException {
        return new JSONObject(sb);
    }
}
