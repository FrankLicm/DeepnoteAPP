package com.example.com.mypplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/**********搜索活动***********/
public class SearchViewActivity extends AppCompatActivity implements AbsListView.OnScrollListener ,Serializable {
    User user=new User();
    List<Note> noteList=new ArrayList<Note>();
    public  final static String SER_KEY = "com.example.com.mypplication";
    private static final long serialVersionUID = 1L;
    private  static SimpleAdapter simpleAdapter;
    ListView notelistview;
    private View moreView;
    private int lastItem;
    private int count=0;
    private Integer popularity;
    private static Handler mHandler;
    List<HashMap<String, String>> notehlist = new ArrayList<HashMap<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra(SER_KEY);
        setContentView(R.layout.activity_search_view);
        notelistview=(ListView)findViewById(R.id.searchlistview);
        setNotelist();
    }
    private void setNotelist()
    {
        searchRunnable r=new searchRunnable();
        r.setKeywords(user.getSearchkeyword());
        r.setPopularity(2000);
        Thread t=new Thread(r);
        t.start();
        try
        {
            t.join();
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (!r.getNotelist().isEmpty()) {
            this.popularity=r.getNextpopularity();
            noteList = r.getNotelist();
            for (Iterator<Note> iterator = noteList.iterator(); iterator.hasNext(); ) {
                Note note = (Note) iterator.next();
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("title", note.getTitle());
                map.put("owner", note.getOwner());
                map.put("prilabel", note.getPrilabel());
                map.put("feel", note.getFeel());
                notehlist.add(map);
            }
            moreView = getLayoutInflater().inflate(R.layout.layout_load, null);
            simpleAdapter = new SimpleAdapter(this, notehlist,
                    R.layout.notechartlist_items, new String[]{"title",
                    "owner", "prilabel", "feel"}, new int[]{R.id.title,
                    R.id.username, R.id.prilabel, R.id.note_content});
            count=noteList.size();
            if(count%5==0) {
                notelistview.addFooterView(moreView);
            }
            notelistview.setAdapter(simpleAdapter);
            notelistview.setOnScrollListener(this);
            try {
                mHandler = new Handler() {
                    public void handleMessage(android.os.Message msg) {
                        switch (msg.what) {
                            case 0:
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                //loadMoreData();  //加载更多数据，这里可以使用异步加载
                                searchRunnable r1=new searchRunnable();
                                Log.d("popularity",popularity.toString());
                                r1.setPopularity(popularity);
                                r1.setKeywords(user.getSearchkeyword());
                                Thread t1=new Thread(r1);
                                t1.start();
                                try
                                {
                                    t1.join();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if(!r1.getNotelist().isEmpty()) {
                                    popularity=r1.getNextpopularity();
                                    noteList.addAll(r1.getNotelist());
                                    for (Iterator<Note> iterator = r1.getNotelist().iterator(); iterator.hasNext(); ) {
                                        Note note = (Note) iterator.next();
                                        HashMap<String, String> map = new HashMap<String, String>();
                                        map.put("title", note.getTitle());
                                        map.put("owner", note.getOwner());
                                        map.put("prilabel", note.getPrilabel());
                                        map.put("feel", note.getFeel());
                                        notehlist.add(map);
                                    }
                                    count = noteList.size();
                                    simpleAdapter.notifyDataSetChanged();
                                    moreView.setVisibility(View.GONE);
                                    if (count > 3000) {
                                        Toast.makeText(SearchViewActivity.this, "木有更多数据！", Toast.LENGTH_SHORT).show();
                                        notelistview.removeFooterView(moreView); //移除底部视图
                                    }
                                    //Log.i(TAG, "加载更多数据");
                                }
                                else {
                                    moreView.setVisibility(View.GONE);
                                    notelistview.removeFooterView(moreView);
                                    Toast.makeText(SearchViewActivity.this, "木有更多数据！", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 1:

                                break;
                            default:
                                break;
                        }
                    }
                };
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            notelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    notereadRunnable r = new notereadRunnable();
                    r.setNoteid(noteList.get(position).getNoteid());
                    Thread t = new Thread(r);
                    t.start();
                    try {
                        t.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (r.result.equals("true")) {
                        user.setpassNote(r.getNote());
                        Intent readIntent = new Intent(SearchViewActivity.this, ReadNoteActivity.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable(SER_KEY, user);
                        readIntent.putExtras(mBundle);
                        startActivity(readIntent);
                    } else if (r.getResult().equals("false")) {
                        new AlertDialog.Builder(SearchViewActivity.this)
                                .setMessage("打开失败:" + r.getFeedback())
                                .setPositiveButton("确定", null)
                                .show();
                    } else {
                        new AlertDialog.Builder(SearchViewActivity.this)
                                .setMessage("打开失败:原因未知")
                                .setPositiveButton("确定", null)
                                .show();
                    }
                }
            });
        }
        else
        {
            SearchViewActivity.this.finish();
            Toast.makeText(getApplicationContext(), "无搜索结果", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        //Log.i(TAG, "scrollState="+scrollState);
        //下拉到空闲是，且最后一个item的数等于数据的总数时，进行更新
        if(lastItem == count  && scrollState == this.SCROLL_STATE_IDLE){
            //Log.i(TAG, "拉到最底部");
            moreView.setVisibility(view.VISIBLE);
            mHandler.sendEmptyMessage(0);
        }

    }
    //声明Handler
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //Log.i(TAG, "firstVisibleItem="+firstVisibleItem+"\nvisibleItemCount="+visibleItemCount+"\ntotalItemCount"+totalItemCount);
        lastItem = firstVisibleItem + visibleItemCount - 1;  //减1是因为上面加了个addFooterView
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
                        Toast.makeText(SearchViewActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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


    class searchRunnable implements Runnable
    {
        String keywords;
        Integer popularity;
        Integer nextpopularity;
        List<Note> notelist =new ArrayList<Note>();

        public Integer getNextpopularity() {
            return nextpopularity;
        }

        public void setNextpopularity(Integer nextpopularity) {
            this.nextpopularity = nextpopularity;
        }

        public List<Note> getNotelist() {
            return notelist;
        }

        public void setNotelist(List<Note> notelist) {
            this.notelist = notelist;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public Integer getPopularity() {
            return popularity;
        }

        public void setPopularity(Integer popularity) {
            this.popularity = popularity;
        }
        //popularity=praisnum*1+commentnum*5
        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/search/NotesbywordsController.php");
                String strResult=new String("");
                JSONArray jarr=null;
                // replace with your url
                request.setHeader("Cookie", user.getSessionid());
                HttpResponse response;
                try {
                    JSONArray jsonArray=new JSONArray();
                    String s=new String("");
                    String [] ss=keywords.split(";");
                    s=s+"[";
                    s=s+"\""+ss[0]+"\"";
                    for (int i=1;i<ss.length;i++)
                    {
                        s=s+","+"\""+ss[i]+"\"";
                    }
                    s=s+"]";
                    Log.d("s",s);
                    NameValuePair pair1 = new BasicNameValuePair("keywords",s);
                    NameValuePair pair2= new BasicNameValuePair("popularity",popularity.toString());
                    List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                    pairList.add(pair1);
                    pairList.add(pair2);
                    HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList, HTTP.UTF_8);
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
                                    newnote.setCommentnum(jsono.getString("commentnum"));
                                    newnote.setPraisenum(jsono.getString("praisenum"));
                                    newnote.setPopularity(Integer.valueOf(newnote.getPraisenum())+ Integer.valueOf(newnote.getCommentnum())*5);
                                    newnote.setTitle(title);
                                    notelist.add(newnote);
                                }
                                Integer minp=1000000;
                                for (int i=0;i<notelist.size();i++)
                                {
                                    if(minp>notelist.get(i).getPopularity())
                                    {
                                        minp=notelist.get(i).getPopularity();
                                    }
                                }
                                nextpopularity=minp;
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(SearchViewActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
}
