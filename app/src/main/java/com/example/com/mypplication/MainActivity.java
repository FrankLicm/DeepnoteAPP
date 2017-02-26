package com.example.com.mypplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/******主界面活动********/
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,Serializable {

    public  final static String SER_KEY = "com.example.com.mypplication";
    private static final long serialVersionUID = 1L;
    public  User user;
    public  List<Note> notes=new ArrayList<Note>();
    public  List<List<Note>>    notes_list=new ArrayList<List<Note>>();
    private  List<String> group_list=new ArrayList<String>();
    private  List<String> item_lt=new ArrayList<String>();
    private  List<List<String>> item_list=new ArrayList<List<String>>();
    private  ExpandableListView expandableListView;
    private  ClassifyExpandableListViewAdapter adapter;
    //private SearchView searchView;
    private ImageButton searchButton;
    private Context mContext;
    FlowLayout mTagLayout, mAddTagLayout;
    EditText mEditText;
    private ArrayList<TagItem> mAddTags = new ArrayList<TagItem>();
    private int MAX_TAG_CNT = 5;
    private ClipBoardReceiver mBoardReceiver;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Properties prop;
    protected int idxTextTag(String text) {
        int mTagCnt = mAddTags.size();
        for (int i = 0; i < mTagCnt; i++) {
            TagItem item = mAddTags.get(i);
            if (text.equals(item.tagText)) {
                return i;
            }
        }
        return -1;
    }
    protected void doDelText(String string) {
        int mTagCnt = mAddTags.size();
        mEditText.setVisibility(View.VISIBLE);
        for (int i = 0; i < mTagCnt; i++) {
            TagItem item = mAddTags.get(i);
            if (string.equals(item.tagText)) {
                mAddTagLayout.removeViewAt(i);
                mAddTags.remove(i);
                if (!item.tagCustomEdit) {
                    mTagLayout.getChildAt(item.idx).setActivated(false);
                }
                return;
            }
        }
    }
    private boolean doAddText(final String str, boolean bCustom, int idx) {
        int tempIdx = idxTextTag(str);
        if (tempIdx >= 0) {
            TagItem item = mAddTags.get(tempIdx);
            item.tagCustomEdit = false;
            item.idx = tempIdx;

            return true;
        }

        int tagCnt = mAddTags.size();
        if (tagCnt == MAX_TAG_CNT) {
            Toast.makeText(MainActivity.this, "最多选择" + MAX_TAG_CNT + "个标签", Toast.LENGTH_SHORT).show();
            return false;
        }

        TagItem item = new TagItem();
        item.tagText = str;
        item.tagCustomEdit = bCustom;
        item.idx = idx;
        mAddTags.add(item);

        final TextView view = (TextView) LayoutInflater.from(this).inflate(
                R.layout.addtag_text, mAddTagLayout, false);
        item.mView = view;
        view.setText(str);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (view.isActivated()) {
                    doDelText(str);
                } else {
                    doResetAddTagsStatus();
                    view.setText(view.getText() + "x");
                    view.setActivated(true);
                }
            }
        });
        mAddTagLayout.addView(view, tagCnt);
        tagCnt++;
        if (tagCnt == MAX_TAG_CNT) {
            mEditText.setVisibility(View.GONE);
        }

        return true;
    }
    protected void doResetAddTagsStatus() {
        int cnt = mAddTags.size();
        for (int i = 0; i < cnt; i++) {
            TagItem item = mAddTags.get(i);
            item.mView.setActivated(false);
            item.mView.setText(item.tagText);
        }
    }
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
        user = (User) getIntent().getSerializableExtra(SER_KEY);
        mContext = this;
        Intent mIntent = new Intent();
        mIntent.setClass(MainActivity.this, ClipBoardService.class);
        mContext.startService(mIntent);
        mBoardReceiver = new ClipBoardReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.dict.ClipBoardReceiver");
        registerReceiver(mBoardReceiver, filter);
        Log.d("service:","started1");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton writeButton = (FloatingActionButton) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setItems(getResources().getStringArray(R.array.ItemArray), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO 自动生成的方法存根
                        System.out.println(arg1);
                        if (arg1 == 0) {
                            user.setClassify(group_list);
                            Intent writeIntent=new Intent(MainActivity.this, WriteNoteActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable(SER_KEY, user);
                           writeIntent.putExtras(mBundle);
                            startActivity(writeIntent);
                            MainActivity.this.finish();
                        } else if(arg1==1){
                            final EditText editText = new EditText(MainActivity.this);
                            EditText editText2 = new EditText(MainActivity.this);
                            final AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                            builder2.setTitle("增加分类");
                            builder2.setMessage("请输入分类名:");
                            builder2.setView(editText);
                            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO 自动生成的方法存根
                                    if (!editText.getText().toString().equals("")) {
                                        addclassRunnable r=new addclassRunnable();
                                        r.setidremark(group_list.size(), editText.getText().toString());
                                        Thread t=new Thread(r);
                                        t.start();
                                        try
                                        {
                                            t.join();
                                        }catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        if (r.result.equals("true")) {
                                            group_list.add(editText.getText().toString());
                                            item_lt.add("");
                                            item_list.add(item_lt);
                                            notes_list.add(new ArrayList<Note>());
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setMessage("添加分类成功")
                                                    .setPositiveButton("确定", null)
                                                    .show();
                                            dialog.dismiss();
                                        }
                                        else if(r.result.equals("false"))
                                        {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setMessage("添加失败:"+r.feedback)
                                                    .setPositiveButton("确定", null)
                                                    .show();
                                            dialog.dismiss();
                                        }
                                        else
                                        {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setMessage("添加失败:未知原因")
                                                    .setPositiveButton("确定", null)
                                                    .show();
                                            dialog.dismiss();
                                        }
                                    } else {
                                        new AlertDialog.Builder(MainActivity.this)
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
                        arg0.dismiss();
                    }
                });
                builder.show();
            }

        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerlayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView username = (TextView) headerlayout.findViewById(R.id.userName);
        username.setText(user.getUsername());
        setNoteList();
        expandableListView = (ExpandableListView) findViewById(R.id.seftnoteListView);
        expandableListView.setGroupIndicator(null);
        // 监听组点击
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @SuppressLint("NewApi")
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (notes_list.get(groupPosition).isEmpty() || notes_list.get(groupPosition).get(0).getTitle().equals("")) {
                    Toast.makeText(MainActivity.this, "此分类下暂无笔记哦", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        // 监听每个分组里子控件的点击事件
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                //Toast.makeText(MainActivity.this, "notetitle:" + notes_list.get(groupPosition).get(childPosition).getTitle() + "  noteid:" + notes_list.get(groupPosition).get(childPosition).getNoteid(), Toast.LENGTH_SHORT).show();

                notereadRunnable r=new notereadRunnable();
                r.setNoteid(notes_list.get(groupPosition).get(childPosition).getNoteid());

                Thread t=new Thread(r);
                t.start();
                try{
                    t.join();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(r.result.equals("true"))
                {
                    user.setpassNote(r.getNote());
                    Intent readIntent=new Intent(MainActivity.this, ReadNoteActivity.class);
                    ReadNoteActivity.SER_KEY=MainActivity.SER_KEY;
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    readIntent.putExtras(mBundle);
                    startActivity(readIntent);
                }
                else if (r.getResult().equals("false"))
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("打开失败:"+r.getFeedback())
                            .setPositiveButton("确定", null)
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("打开失败:原因未知")
                            .setPositiveButton("确定", null)
                            .show();
                }
                return false;
            }
        });

        adapter = new ClassifyExpandableListViewAdapter(this);

        expandableListView.setAdapter(adapter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //searchView=(SearchView)findViewById(R.id.searchselfView);
        //searchView.setIconifiedByDefault(false);
        //searchView.setSubmitButtonEnabled(true);
        //searchView.setQueryHint("查找笔记");
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals("")) {
                    user.setSearchkeyword(query);
                    Intent searchIntent = new Intent(MainActivity.this, SearchViewActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    searchIntent.putExtras(mBundle);
                    startActivity(searchIntent);
                } else {
                    return false;
                }
                return true;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
           */
        mAddTagLayout = (FlowLayout) findViewById(R.id.addtag_layout);
        mEditText = (EditText) findViewById(R.id.add_edit);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = mEditText.getEditableText().toString().trim();
                    if (text.length() > 0) {
                        if (idxTextTag(text) < 0) {
                            doAddText(text, true, -1);
                        }
                        mEditText.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
        searchButton=(ImageButton)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAddTags.size()>0)
                {
                    String query;
                    if(mAddTags.size()==1)
                    {
                        query=mAddTags.get(0).tagText;
                    }
                    else {
                        query=mAddTags.get(0).tagText;
                        for (int i = 1; i < mAddTags.size(); i++) {
                            query =query+";"+mAddTags.get(i).tagText;
                        }
                    }
                    user.setSearchkeyword(query);
                    SearchViewActivity m=new SearchViewActivity();
                    user.setActivity(m);
                    Intent mainIntent = new Intent(MainActivity.this, LoadingActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    mainIntent.putExtras(mBundle);
                    startActivity(mainIntent);
                }
                else
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("请输入搜索关键词再搜索!")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBoardReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


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
            Intent mainIntent = new Intent(MainActivity.this, LoadingActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(SER_KEY, user);
            mainIntent.putExtras(mBundle);
            startActivity(mainIntent);
        } else if(id==R.id.firstpage) {

        }else if (id == R.id.nav_draft) {
             //this.setContentView();
        } else if (id == R.id.nav_finding) {
            NoteChartActivity m=new NoteChartActivity();
            user.setActivity(m);
            Intent mainIntent = new Intent(MainActivity.this, LoadingActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(SER_KEY, user);
            mainIntent.putExtras(mBundle);
            startActivity(mainIntent);
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_logout) {
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
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
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("退出登录成功")
                        .setPositiveButton("确定", null)
                        .show();
                prop = loadConfig(MainActivity.this, "/mnt/sdcard/config.properties");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//配置文件不存在的时候创建配置文件 初始化配置信息
                prop = new Properties();
                prop.put("ip", Config.ip);
                prop.put("name","");
                prop.put("pwd", "");
                saveConfig(MainActivity.this, "/mnt/sdcard/config.properties", prop);
                //group_list.clear();
                //notes_list.clear();
                //notes.clear();
                user.clear();
                MainActivity.this.finish();
            } else if (r.result.equals("false")) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("退出登录失败")
                        .setMessage(r.feedback)
                        .setPositiveButton("确定", null)
                        .show();
            } else {
                new AlertDialog.Builder(MainActivity.this)
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
    public void onStart() {
        super.onStart();

        // ATTENTION: This wares auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.com.mypplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.com.mypplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
    public void setNoteList()
    {
        makenotepostrequest();
        for(int i=0;i<group_list.size();i++)
        {
            notegetRunnable r=new notegetRunnable();
            r.setClassifyid(i);
            Thread t=new Thread(r);
            t.start();
            try{
                t.join();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    public void makenotepostrequest()
    {
         notegroupgetRunnable r=new notegroupgetRunnable();
        Thread t=new Thread(r);
        t.start();
        try {
            t.join();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
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
                        Toast.makeText(MainActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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


    class notegetRunnable implements Runnable{
        public Integer classifyid;
        public void setClassifyid(int classifyid)
        {
            this.classifyid=classifyid;
        }
        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/note/ClassifyNotesController.php");
                String strResult=new String("");
                JSONArray jarr=null;
                // replace with your url
                    request.setHeader("Cookie", user.getSessionid());
                HttpResponse response;
                try {
                    NameValuePair pair = new BasicNameValuePair("classifyid",classifyid.toString());
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
                                    /**取出list下的name的值 **/
                                    String title= jsono.getString("title");
                                    Note newnote=new Note();

                                    newnote.setNoteid(jsono.getString("id"));
                                    newnote.setFeel(jsono.getString("feel"));
                                    newnote.setPrilabel(jsono.getString("prilabel"));
                                    newnote.setTitle(title);
                                    notes.add(newnote);
                                    item_lt.add(title);
                                }
                                List<String> item=new ArrayList<String>();
                                for (int i=0;i<item_lt.size();i++)
                                {
                                    item.add(item_lt.get(i));
                                }
                                item_list.add(item);
                                item_lt.clear();
                                List<Note>note=new ArrayList<Note>();
                                for (int i=0;i<notes.size();i++)
                                {
                                    note.add(notes.get(i));
                                }
                                notes_list.add(note);
                                notes.clear();
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(MainActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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



    class addclassRunnable implements Runnable{
        Integer classifyid;
        String remark;
        String result;
        String feedback;
        public void setidremark(Integer classifyid,String remark)
        {
            this.classifyid=classifyid;
            this.remark=remark;
        }
        @Override
        public void run() {
            NameValuePair pair1 = new BasicNameValuePair("classifyid", classifyid.toString());
            NameValuePair pair2 = new BasicNameValuePair("remark", remark);
            Log.d("remark",remark);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair1);
            pairList.add(pair2);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList, HTTP.UTF_8);
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/action/AddNoteGroupController.php");
                request.setEntity(requestHttpEntity);
                String strResult;
                JSONObject jsonObject = null;
                // replace with your url
                    request.setHeader("Cookie", user.getSessionid());
                request.setHeader("Accept-Encoding", "gzip, deflate");
                request.setHeader(" Accept-Language","zh-CN,zh;q=0.8");
                request.setHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
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
                        Toast.makeText(MainActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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




    class notegroupgetRunnable implements Runnable{

        //public String[] result = new String[];
        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/note/NoteGroupController.php");
                String strResult=new String("");
                //JSONObject jsonObject = null;
                JSONArray jarr=null;
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
                        String names = "";
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
                                    /**取出list下的name的值 **/
                                    String id = jsono.getString("classifyid");
                                    String remark = jsono.getString("remark");
                                    Log.d("id", id);
                                    Log.d("remark", remark);
                                    group_list.add(remark);
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(MainActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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

    class ClassifyExpandableListViewAdapter extends BaseExpandableListAdapter {
        private Context context;

        public ClassifyExpandableListViewAdapter(Context context) {
            this.context = context;
        }

        public ClassifyExpandableListViewAdapter() {
            super();
        }


        @Override
        public int getGroupCount() {
            return group_list.size();
        }


        @Override
        public int getChildrenCount(int groupPosition) {
            return item_list.get(groupPosition).size();
        }


        @Override
        public Object getGroup(int groupPosition) {return group_list.get(groupPosition);
        }


        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return item_list.get(groupPosition).get(childPosition);
        }


        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }


        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }


        @Override
        public boolean hasStableIds() {
            return true;
        }


        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder groupHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.expendlist_group, null);
                groupHolder = new GroupHolder();
                groupHolder.txt = (TextView) convertView.findViewById(R.id.txt);
                groupHolder.img=(ImageView)convertView.findViewById(R.id.img);
                convertView.setTag(groupHolder);
            } else {
                groupHolder = (GroupHolder) convertView.getTag();
            }
            if (!isExpanded) {
                groupHolder.img.setBackgroundResource(R.drawable.arrow_right);
            } else {
                groupHolder.img.setBackgroundResource(R.drawable.arrow_down);
            }
            groupHolder.txt.setText(group_list.get(groupPosition));
            return convertView;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            /*ItemHolder itemHolder = null;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(context).inflate(R.layout.expendlist_item, null);
                itemHolder = new ItemHolder();
                itemHolder.txt = (TextView)convertView.findViewById(R.id.title);
                //itemHolder.img = (ImageView)convertView.findViewById(R.id.img);
                convertView.setTag(itemHolder);
            }
            else
            {
                itemHolder = (ItemHolder)convertView.getTag();
            }
            itemHolder.txt.setText(item_list.get(groupPosition).get(childPosition));
            //itemHolder.img.setBackgroundResource(item_list2.get(groupPosition).get(childPosition));*/
            convertView = LayoutInflater.from(context).inflate(R.layout.expendlist_item, null);
            TextView title=(TextView)convertView.findViewById(R.id.title);
            TextView username=(TextView)convertView.findViewById(R.id.username);
            TextView note_content=(TextView)convertView.findViewById(R.id.note_content);
            TextView prilabel=(TextView)convertView.findViewById(R.id.prilabel);
            title.setText(notes_list.get(groupPosition).get(childPosition).getTitle());
            username.setText(notes_list.get(groupPosition).get(childPosition).getOwner());
            note_content.setText(notes_list.get(groupPosition).get(childPosition).getFeel());
            prilabel.setText(notes_list.get(groupPosition).get(childPosition).getPrilabel());
            return convertView;
        }
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
    class GroupHolder
    {
        public TextView txt;
        public ImageView img;
    }
    class ClipBoardReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String value = (String) bundle.get("clipboardvalue");
                //Intent show = new Intent(MainActivity.this, FloatingWindowService.class);
                //show.putExtra(FloatingWindowService.OPERATION,FloatingWindowService.OPERATION_SHOW);
                //show.putExtra("copyValue", value);
                Intent intent1=new Intent(MainActivity.this, FloatingWindowService.class);
                Bundle mBundle = new Bundle();
                user.setClassify(group_list);
                user.setOperation(FloatingWindowService.OPERATION_SHOW);
                user.setCopyvalue(value);
                mBundle.putSerializable(SER_KEY, user);
                intent1.putExtras(mBundle);
                MainActivity.this.startService(intent1);
                Log.d("service","started");
            }
        }
    }
}
