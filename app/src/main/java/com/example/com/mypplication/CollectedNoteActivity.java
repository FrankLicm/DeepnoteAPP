package com.example.com.mypplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/********收藏笔记活动******/
public class CollectedNoteActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Serializable {
    public  final static String SER_KEY = "com.example.com.mypplication";
    private static final long serialVersionUID = 1L;
    public static User user;
    public static List<Note> noteList=new ArrayList<Note>();
    private  static SimpleAdapter simpleAdapter;
    ListView notelistview;
    private Properties prop;
    public Properties loadConfig(Context context, String file) {
        Properties properties = new Properties();
        try {
            FileInputStream s = new FileInputStream(file);
            properties.load(s);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return properties;
    }
    //保存配置文件
    public boolean saveConfig(Context context, String file, Properties properties) {
        try {
            File fil=new File(file);
            if(!fil.exists())
                fil.createNewFile();
            FileOutputStream s = new FileOutputStream(fil);
            properties.store(s, "");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SysApplication.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra(SER_KEY);
        setContentView(R.layout.activity_collected_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerlayout = navigationView.inflateHeaderView(R.layout.nav_header_collected_note);
        TextView username = (TextView) headerlayout.findViewById(R.id.userName);
        username.setText(user.getUsername());
        notelistview=(ListView)findViewById(R.id.noteListView);
        setNoteList();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            noteList.clear();
            CollectedNoteActivity.this.finish();
            return true;
                }else{
             return super.onKeyDown(keyCode, event);}
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.collected_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_treasure) {
            //this.setContentView();
            // Handle the camera action
        }else if(id==R.id.firstpage) {
            noteList.clear();
           CollectedNoteActivity.this.finish();
        }
        else if (id == R.id.nav_draft) {

            //this.setContentView();
        } else if (id == R.id.nav_finding) {
            NoteChartActivity m=new NoteChartActivity();
            user.setActivity(m);
            Intent mainIntent = new Intent(CollectedNoteActivity.this, LoadingActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(SER_KEY, user);
            mainIntent.putExtras(mBundle);
            startActivity(mainIntent);
            noteList.clear();
            CollectedNoteActivity.this.finish();
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_logout) {
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(CollectedNoteActivity.this);
            //builder2.setTitle("增加分类");
            builder2.setMessage("真的要退出登录吗?*_*");
            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
            logoutRunnable r = new logoutRunnable();
            Thread t = new Thread(r);
            t.start();
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (r.result.equals("true")) {
                user.setSessionid("");
                new AlertDialog.Builder(CollectedNoteActivity.this)
                        .setMessage("退出登录成功")
                        .setPositiveButton("确定", null)
                        .show();
                prop = loadConfig(CollectedNoteActivity.this, "/mnt/sdcard/config.properties");
//配置文件不存在的时候创建配置文件 初始化配置信息
                prop = new Properties();
                prop.put("ip", Config.ip);
                prop.put("name","");
                prop.put("pwd", "");
                saveConfig(CollectedNoteActivity.this, "/mnt/sdcard/config.properties", prop);
                noteList.clear();
                startActivity(new Intent(CollectedNoteActivity.this, LoginActivity.class));
                SysApplication.getInstance().exit();
            } else if (r.result.equals("false")) {
                new AlertDialog.Builder(CollectedNoteActivity.this)
                        .setTitle("退出登录失败")
                        .setMessage(r.feedback)
                        .setPositiveButton("确定", null)
                        .show();
            } else {
                new AlertDialog.Builder(CollectedNoteActivity.this)
                        .setTitle("退出登录失败")
                        .setMessage("未知原因，请检查网络或联系制作者")
                        .setPositiveButton("确定", null)
                        .show();
            }
                }});
            builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // TODO 自动生成的方法存根
                    dialog.dismiss();
                }
            });
            builder2.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void setNoteList()
    {
        collectedRunnable r=new collectedRunnable();
        Thread t=new Thread(r);
        t.start();
        try
        {
            t.join();
        }catch (Exception e) {
            e.printStackTrace();
        }
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
                R.layout.collected_items, new String[]{"title",
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
                    Intent readIntent=new Intent(CollectedNoteActivity.this, ReadNoteActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    readIntent.putExtras(mBundle);
                    startActivity(readIntent);
                }
                else if (r.getResult().equals("false"))
                {
                    new AlertDialog.Builder(CollectedNoteActivity.this)
                            .setMessage("打开失败:"+r.getFeedback())
                            .setPositiveButton("确定", null)
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(CollectedNoteActivity.this)
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
                            Log.d("strResult",strResult);
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
                        Toast.makeText(CollectedNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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



    class collectedRunnable implements  Runnable {
        @Override
        public void run() {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://" + Config.ip + "/deepnote/controller/note/NoteCollectedController.php");
            String strResult = new String("");
            JSONArray jarr = null;
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
                        Log.d("strResult", strResult);
                        jarr = new JSONArray(strResult);
                        //jsonObject = getJSON(strResult);
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
                        //String groups=jsonObject.getString("groups");
                        //JSONObject jGroups=new JSONObject(groups);
                        if (!strResult.equals("")) {
                            for (int i = 0; i < jarr.length(); i++) {

                                /** **/
                                JSONObject jsono = (JSONObject) jarr.get(i);
                                /**取出list下的name的值 **/
                                String title = jsono.getString("title");
                                Note newnote = new Note();

                                newnote.setNoteid(jsono.getString("id"));
                                newnote.setFeel(jsono.getString("feel"));
                                newnote.setPrilabel(jsono.getString("prilabel"));
                                newnote.setTitle(title);
                                newnote.setOwner(jsono.getString("owner"));
                                noteList.add(newnote);
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(CollectedNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                Log.d("wrong:", "wrong");
                //e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("wrong2:", "wrong2");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class logoutRunnable implements Runnable {
        public String result = new String("");
        public String feedback = new String("");

        @Override
        public void run() {

            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/admin/LogoffController.php");
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
                        String names = "";
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
                        Toast.makeText(CollectedNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
