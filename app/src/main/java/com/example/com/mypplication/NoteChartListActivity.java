package com.example.com.mypplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/*****笔记排名表****/
public class NoteChartListActivity extends AppCompatActivity {
    User user=new User();
    List<Note>noteList=new ArrayList<Note>();
    public  final static String SER_KEY = "com.example.com.mypplication";
    private  static SimpleAdapter simpleAdapter;
    ListView notelistview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra(SER_KEY);
        noteList=user.getPassnotelist();
        setContentView(R.layout.activity_note_chart_list);
        notelistview=(ListView)findViewById(R.id.noteListView);
            setNotelist();
    }
    public void setNotelist()
    {
        List<HashMap<String, String>> notehlist = new ArrayList<HashMap<String, String>>();
        for (Iterator<Note> iterator = noteList.iterator(); iterator.hasNext();)
        {
            Note note=(Note)iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title",note.getTitle());
            map.put("owner",note.getOwner());
            map.put("prilabel",note.getPrilabel());
            map.put("feel",note.getFeel());
            notehlist.add(map);
        }
        simpleAdapter=new SimpleAdapter(this, notehlist,
                R.layout.notechartlist_items, new String[]{"title",
                "owner", "prilabel","feel"}, new int[]{R.id.title,
                R.id.username, R.id.prilabel,R.id.note_content});
        notelistview.setAdapter(simpleAdapter);
        notelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                notereadRunnable r=new notereadRunnable();
                r.setNoteid(noteList.get(position).getNoteid());
                Thread t=new Thread(r);
                t.start();
                try
                {
                    t.join();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(r.result.equals("true"))
                {
                    user.setpassNote(r.getNote());
                    Intent readIntent=new Intent(NoteChartListActivity.this, ReadNoteActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    readIntent.putExtras(mBundle);
                    startActivity(readIntent);
                }
                else if (r.getResult().equals("false"))
                {
                    new AlertDialog.Builder(NoteChartListActivity.this)
                            .setMessage("打开失败:" + r.getFeedback())
                            .setPositiveButton("确定", null)
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(NoteChartListActivity.this)
                            .setMessage("打开失败:原因未知")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
    }
    class notereadRunnable implements Runnable{
        public String noteid;
        public String result;
        public String feedback;
        public Note noted=new Note();
        public void setNoteid(String noteid){this.noteid=noteid;}
        public String getResult() {
            return result;
        }

        public String getFeedback() {
            return feedback;
        }

        public Note getNote() {
            return noted;
        }

        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/note/NoteDetailController.php");
                String strResult=new String("");
                JSONArray jarr=null;
                JSONObject jsonObject=null;
                // replace with your url
                //if (!user.getSessionid().equals("")) {
                request.setHeader("Cookie", user.getSessionid());
                //}
                HttpResponse response;
                try {
                    NameValuePair pair = new BasicNameValuePair("noteid",noteid);
                    List<NameValuePair> pairList = new ArrayList<NameValuePair>();

                    pairList.add(pair);
                    HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                    request.setEntity(requestHttpEntity);
                    response = client.execute(request);
                    //Log.d("postresponse",response);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        try {
                            /**读取服务器返回过来的json字符串数据**/
                            strResult = EntityUtils.toString(response.getEntity());
                            Log.d("strResult", strResult);
                            //jarr = new JSONArray(strResult);
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
                        String names = "";
                        try {
                            /**
                             * jsonObject.getString("code") 取出code
                             * 比如这里返回的json 字符串为 [code:0,msg:"ok",data:[list:{"name":1},{"name":2}]]
                             * **/

                            /**得到data这个key**/
                            result=jsonObject.getString("result");
                            if(result.equals("true"))
                            {
                                String detail=jsonObject.getString("detail");
                                Log.d("detail", detail);
                                JSONObject object=new JSONObject(detail);
                                noted.setTitle(object.getString("title"));
                                noted.setNoteid(object.getString("id"));
                                noted.setOwerid(object.getString("ownerid"));
                                Log.d("noteid", noted.getOwerid());
                                noted.setTime(object.getString("time"));
                                noted.setSource(object.getString("source"));
                                noted.setSourcewriter(object.getString("sourcewriter"));
                                noted.setLink(object.getString("link"));
                                noted.setRef(object.getString("ref"));
                                noted.setFeel(object.getString("feel"));
                                noted.setPrilabel(object.getString("prilabel"));
                                noted.setPraisenum(object.getString("praisenum"));
                                noted.setOwner(object.getString("owner"));
                                String labels=object.getString("labels");
                                JSONArray labelsa=new JSONArray(labels);
                                String [] s=new String[labelsa.length()];
                                for(int i=0;i<labelsa.length();i++)
                                {
                                    s[i]=(String)labelsa.get(i);
                                }
                                noted.setLabels(s);
                                String comments=object.getString("comments");
                                Log.d("comment",comments);
                                JSONArray commentsa=new JSONArray(comments);
                                List<Comment> comment=new ArrayList<Comment>();
                                for(int i=0;i<commentsa.length();i++)
                                {
                                    JSONObject ja=(JSONObject)commentsa.get(i);
                                    Comment c=new Comment();
                                    c.setUsername(ja.getString("username"));
                                    c.setTime(ja.getString("time"));
                                    c.setContent(ja.getString("content"));
                                    comment.add(c);
                                }
                                Integer num=new Integer(commentsa.length());
                                noted.setCommentnum(num.toString());
                                noted.setComments(comment);
                            }
                            else if(result.equals("false"))
                            {
                                feedback=jsonObject.getString("feedback");
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(NoteChartListActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong:", "wrong");
                    //e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong2:", "wrong2");
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public JSONObject getJSON(String sb) throws JSONException {
        return new JSONObject(sb);
    }
}
