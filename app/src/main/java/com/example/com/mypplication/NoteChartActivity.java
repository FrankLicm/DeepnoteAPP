package com.example.com.mypplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/*****笔记排名活动*****/
public class NoteChartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,Serializable {
    public  final static String SER_KEY = "com.example.com.mypplication";
    private static final long serialVersionUID = 1L;
    public static User user;
    public static List<Kind> kindList=new ArrayList<Kind>();
    public static List<String>paths=new ArrayList<String>();
    public static List<String>pname=new ArrayList<String>();
    private  static SimpleAdapter simpleAdapter;
    ListView kindlistview;
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
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);
        user = (User) getIntent().getSerializableExtra(SER_KEY);
        setContentView(R.layout.activity_note_chart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerlayout = navigationView.inflateHeaderView(R.layout.nav_header_note_chart);
        TextView username = (TextView) headerlayout.findViewById(R.id.userName);
        username.setText(user.getUsername());
        kindlistview=(ListView)findViewById(R.id.kindListView);
        setKindList();
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
        getMenuInflater().inflate(R.menu.note_chart, menu);
        return true;
    }
    public void setKindList()
    {
        getcharttypesRunnable r=new getcharttypesRunnable();
        Thread t=new Thread(r);
        t.start();
        try
        {
            t.join();
        }catch (Exception e) {
            e.printStackTrace();
        }
        List<Bitmap>bitmaps=new ArrayList<Bitmap>();
        for(int i=0;i<paths.size();i++)
        {
            String path= Environment.getExternalStorageDirectory()+"/deepnote/"+pname.get(i);
            File mFile=new File(path);
            //若该文件存在
            if (mFile.exists()) {
                long   time=mFile.lastModified();
                Log.d("filetime",""+time);
                Log.d("filetime",""+System.currentTimeMillis());
                if(System.currentTimeMillis()-time<604800000) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    bitmaps.add(bitmap);
                }
                else
                {
                    getchartpictureRunnable r1 = new getchartpictureRunnable();
                    r1.setPath(paths.get(i));
                    r1.setName(pname.get(i));
                    Thread t1 = new Thread(r1);
                    t1.start();
                    try {
                        t1.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bitmaps.add(r1.getPic());
                }
            }
            else {
                getchartpictureRunnable r1 = new getchartpictureRunnable();
                r1.setPath(paths.get(i));
                r1.setName(pname.get(i));
                Thread t1 = new Thread(r1);
                t1.start();
                try {
                    t1.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bitmaps.add(r1.getPic());
            }

        }
        List<HashMap<String, Object>> kindhlist = new ArrayList<HashMap<String, Object>>();
        int i=0;
        for (Iterator<Kind> iterator = kindList.iterator(); iterator.hasNext();)
        {
            Kind kind=(Kind)iterator.next();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name",kind.getName());
            map.put("img",bitmaps.get(i));
            kindhlist.add(map);
            i++;
        }
        simpleAdapter=new SimpleAdapter(this, kindhlist,
                R.layout.notechart_items, new String[]{"img","name"}, new int[]{R.id.kindpicture,R.id.kindname});
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                // TODO Auto-generated method stub
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView i = (ImageView)view;
                    i.setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
            }} );
        kindlistview.setAdapter(simpleAdapter);
        kindlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getchartnotesRunnable r = new getchartnotesRunnable();
                r.setKindid(kindList.get(position).getKindid());
                Thread t = new Thread(r);
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                user.setPassnotelist(r.getNotelist());
                Intent chartIntent = new Intent(NoteChartActivity.this, NoteChartListActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable(SER_KEY, user);
                chartIntent.putExtras(mBundle);
                startActivity(chartIntent);
            }
        });
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
            CollectedNoteActivity m=new CollectedNoteActivity();
            user.setActivity(m);
            Intent mainIntent = new Intent(NoteChartActivity.this, LoadingActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(SER_KEY, user);
            mainIntent.putExtras(mBundle);
            startActivity(mainIntent);
            paths.clear();
            pname.clear();
            kindList.clear();
            NoteChartActivity.this.finish();
        }else if(id==R.id.firstpage) {
            //noteList.clear();
            paths.clear();
            pname.clear();
            kindList.clear();
            NoteChartActivity.this.finish();
        }
        else if (id == R.id.nav_draft) {

            //this.setContentView();
        } else if (id == R.id.nav_finding) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_logout) {
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(NoteChartActivity.this);
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
                new AlertDialog.Builder(NoteChartActivity.this)
                        .setMessage("退出登录成功")
                        .setPositiveButton("确定", null)
                        .show();
                prop = loadConfig(NoteChartActivity.this, "/mnt/sdcard/config.properties");
//配置文件不存在的时候创建配置文件 初始化配置信息
                prop = new Properties();
                prop.put("ip", Config.ip);
                prop.put("name","");
                prop.put("pwd", "");
                saveConfig(NoteChartActivity.this, "/mnt/sdcard/config.properties", prop);
                startActivity(new Intent(NoteChartActivity.this, LoginActivity.class));
                kindList.clear();
                paths.clear();
                pname.clear();
                SysApplication.getInstance().exit();
            } else if (r.result.equals("false")) {
                new AlertDialog.Builder(NoteChartActivity.this)
                        .setTitle("退出登录失败")
                        .setMessage(r.feedback)
                        .setPositiveButton("确定", null)
                        .show();
            } else {
                new AlertDialog.Builder(NoteChartActivity.this)
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            kindList.clear();
            NoteChartActivity.this.finish();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);}
    }
    class getchartpictureRunnable implements Runnable{
        Bitmap pic;
        public Bitmap getPic() {
            return pic;
        }
        String pname;
        String path;
        public void setName(String name) {this.pname = name;}
        public void setPath(String path) {
            this.path = path;
        }
        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(path);
                // replace with your url
                request.setHeader("Cookie", user.getSessionid());
                HttpResponse response;
                try {
                    response = client.execute(request);
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    pic = BitmapFactory.decodeStream(is);
                    Log.e("tag", "保存图片");
                    File f = new File(Environment.getExternalStorageDirectory()+"/deepnote/");
                    if(!f.exists())
                        f.mkdir();
                    File file=new File(f,pname);
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    pic.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    try {
                        fOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.d("wrong:", "wrong");
                    //e.printStackTrace();
                    //Your code goes here
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    class getchartnotesRunnable implements Runnable{
        String kindid=new String("");
        List<Note> notelist =new ArrayList<Note>();
        public String getKindid() {
            return kindid;
        }

        public void setKindid(String kindid) {
            this.kindid = kindid;
        }

        public List<Note> getNotelist() {
            return notelist;
        }

        public void setNotelist(List<Note> notelist) {
            this.notelist = notelist;
        }

        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/chart/ChartController.php");
                String strResult=new String("");
                JSONArray jarr=null;
                // replace with your url
                request.setHeader("Cookie", user.getSessionid());
                HttpResponse response;
                try {
                    NameValuePair pair = new BasicNameValuePair("kindid",kindid.toString());
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
                            if(!strResult.equals("")) {
                                for (int i = 0; i < jarr.length(); i++) {
                                    /** **/
                                    JSONObject jsono = (JSONObject) jarr.get(i);
                                    /**取出list下的name的值**/
                                    String title= jsono.getString("title");
                                    Note newnote=new Note();
                                    newnote.setNoteid(jsono.getString("id"));
                                    newnote.setFeel(jsono.getString("feel"));
                                    newnote.setPrilabel(jsono.getString("prilabel"));
                                    newnote.setTitle(title);
                                    notelist.add(newnote);
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(NoteChartActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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

    class getcharttypesRunnable implements  Runnable {
        @Override
        public void run() {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://" + Config.ip + "/deepnote/controller/chart/ChartTypeController.php");
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
                                Kind newkind = new Kind();
                                newkind.setKindid(jsono.getString("kindid"));
                                newkind.setName(jsono.getString("name"));
                                paths.add("http://" + Config.ip + "/deepnote/pictures/charttype/" + jsono.getString("path"));
                                pname.add(jsono.getString("path"));
                                Log.d("path", jsono.getString("path"));
                                kindList.add(newkind);
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(NoteChartActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(NoteChartActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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

