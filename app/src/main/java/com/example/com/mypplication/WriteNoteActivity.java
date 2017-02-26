package com.example.com.mypplication;


import android.app.AlertDialog;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*****写笔记活动******/
public class WriteNoteActivity extends AppCompatActivity {
    public static Note note=new Note();
    public static User user=new User();
    FlowLayout mTagLayout, mAddTagLayout;
    EditText mEditText;
    private ArrayList<TagItem> mAddTags = new ArrayList<TagItem>();
    private int MAX_TAG_CNT = 5;
    public  final static String SER_KEY = "com.example.com.mypplication";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra(MainActivity.SER_KEY);
        setContentView(R.layout.activity_write_note);
        note.setClassify(0);
        final EditText title=(EditText)findViewById(R.id.title);
        final TextView spintext=(TextView)findViewById(R.id.spintextview);
        final EditText feel=(EditText)findViewById(R.id.feel);
        final EditText source=(EditText)findViewById(R.id.source);
        if(!user.getCopyvalue().equals(""))
        {
            source.setText(user.getCopyvalue());
        }
        Spinner classify=(Spinner)findViewById(R.id.classify);
        Switch ispublic=(Switch)findViewById(R.id.ispublic);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, user.getClassify());
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        classify.setAdapter(adapter);
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中
        classify.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入myTextView 中*/
                //myTextView.setText("您选择的是："+ adapter.getItem(arg2));
                /* 将mySpinner 显示*/
                spintext.setText(user.getClassify().get(arg2));
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                //myTextView.setText("NONE");
                arg0.setVisibility(View.VISIBLE);
            }
        });
        ispublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    note.setAuth("0");
                } else {
                    note.setAuth("1");
                }
            }
        });
        //note.setAuth("0");
        //note.setClassify(1);
        //note.setTitle("评吕神1");
        //note.setSource("战国策");
        //note.setSourcewriter("未知");
        //note.setFeel("吕神是一个名垂千古的伟大将将军");
        //note.setLink("www.baidu.com");
        //note.setRef("吕神乃秦国之大将，能文能武");
        //note.setPrilabel("吕神");
        //String [] s={"将军","战国"};
        //note.setLabels(s);
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
        ImageButton b=(ImageButton)findViewById(R.id.saveButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note.setTitle(title.getText().toString());
                if(mAddTags.size()>0)
                note.setPrilabel(mAddTags.get(0).tagText);
                if(mAddTags.size()>1)
                {
                    String [] ss=new String[mAddTags.size()-1];
                    for(int i=1;i<mAddTags.size();i++)
                        ss[i-1]=mAddTags.get(i).tagText;
                    note.setLabels(ss);
                }
                note.setClassify(user.getClassify().indexOf(spintext.getText().toString()));
                note.setFeel(feel.getText().toString());
                note.setRef(source.getText().toString());
                save();
            }
        });
    }
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
            Toast.makeText(WriteNoteActivity.this, "最多选择" + MAX_TAG_CNT + "个标签", Toast.LENGTH_SHORT).show();
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent mainIntent=new Intent(WriteNoteActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(SER_KEY, user);
            mainIntent.putExtras(mBundle);
            startActivity(mainIntent);
            user.setCopyvalue("");
            WriteNoteActivity.this.finish();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);}
    }
   public void save(){
       writenoteRunnable r=new writenoteRunnable();
       Thread t=new Thread(r);
       t.start();
       try{
           t.join();
       }catch (Exception e)
       {
           e.printStackTrace();
       }
       if (r.result.equals("true"))
       {
           new AlertDialog.Builder(WriteNoteActivity.this)
                   .setMessage("保存正常")
                   .setPositiveButton("确定", null)
                   .show();
           MainActivity m=new MainActivity();
           user.setActivity(m);
           user.setMainflag(1);
           Intent mainIntent = new Intent(WriteNoteActivity.this, LoadingActivity.class);
           Bundle mBundle = new Bundle();
           mBundle.putSerializable(SER_KEY, user);
           mainIntent.putExtras(mBundle);
           startActivity(mainIntent);
           user.setCopyvalue("");
           WriteNoteActivity.this.finish();
       }
       else if(r.result.equals("false"))
       {
           new AlertDialog.Builder(WriteNoteActivity.this)
                   .setMessage("保存失败:"+r.feedback)
                   .setPositiveButton("确定", null)
                   .show();
       }
       else
       {
           new AlertDialog.Builder(WriteNoteActivity.this)
                   .setMessage("保存失败，未知原因")
                   .setPositiveButton("确定", null)
                   .show();
       }
   }
    class writenoteRunnable implements Runnable{
        String result;
        String feedback;
        @Override
        public void run() {
            NameValuePair Pair1= new BasicNameValuePair("title",note.getTitle());
            NameValuePair Pair2= new BasicNameValuePair("auth",note.getAuth());
            NameValuePair Pair3= new BasicNameValuePair("classify",note.getClassify().toString());
            NameValuePair Pair4= new BasicNameValuePair("source",note.getSource());
            NameValuePair Pair5= new BasicNameValuePair("sourcewriter",note.getSourcewriter());
            NameValuePair Pair6= new BasicNameValuePair("link",note.getLink());
            NameValuePair Pair7= new BasicNameValuePair("ref",note.getRef());
            NameValuePair Pair8= new BasicNameValuePair("feel",note.getFeel());
            NameValuePair Pair9= new BasicNameValuePair("prilabel",note.getPrilabel());
            //note.getLabels();
            JSONArray jsonArray=new JSONArray();
            String s=new String("");
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(Pair1);
            pairList.add(Pair2);
            pairList.add(Pair3);
            pairList.add(Pair4);
            pairList.add(Pair5);
            pairList.add(Pair6);
            pairList.add(Pair7);
            pairList.add(Pair8);
            pairList.add(Pair9);
            if(note.getLabels().length>0) {
                s = s + "[";
                for (int i = 0; i < note.getLabels().length - 1; i++) {
                    s = s + "\"" + note.getLabels()[i] + "\"" + ",";
                }
                s = s + "\"" + note.getLabels()[note.getLabels().length - 1] + "\"]";
                //Log.d("s",s);
                //jsonArray.put(note.getLabels());

                NameValuePair Pair10 = new BasicNameValuePair("labels", s);
                pairList.add(Pair10);
            }
            try{
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList, HTTP.UTF_8);
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/note/WriteNoteController.php");
                request.setEntity(requestHttpEntity);
                String strResult;
                JSONObject jsonObject = null;
                    request.setHeader("Cookie", user.getSessionid());
                request.setHeader("Accept-Encoding","gzip, deflate");
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
                            Toast.makeText(WriteNoteActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
            }catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }
    public JSONObject getJSON(String sb) throws JSONException {
        return new JSONObject(sb);
    }
}
