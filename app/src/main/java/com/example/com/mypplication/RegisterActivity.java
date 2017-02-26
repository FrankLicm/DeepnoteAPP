package com.example.com.mypplication;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.Header;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
/********注册活动******/
public class RegisterActivity extends AppCompatActivity {
    public static User user = new User();
    public static EditText ruserEdit;
    public static EditText rpswdEdit;
    public static EditText rvfcdEdit;
    public static ImageView rvfcddraw;
    public static ImageButton recbutton;
    public static ImageButton registbutton;
    public static ImageButton backbutton;
    public static EditText invitor;
    public static EditText invitecode;
    public static Bitmap pic;
    public static String session = new String("");
    public static String verifycode=new String("");
    public static String result=new String("");
    public static String feedback=new String("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ruserEdit = (EditText) findViewById(R.id.ruser);
        rpswdEdit = (EditText) findViewById(R.id.rpswd);
        rvfcdEdit = (EditText) findViewById(R.id.rvfcde);
        invitor=(EditText)findViewById(R.id.invitor);
        invitecode=(EditText)findViewById(R.id.invitecode);
       rvfcddraw = (ImageView) findViewById(R.id.rvfcdd);
        recbutton = (ImageButton) findViewById(R.id.recbutton);
        registbutton = (ImageButton) findViewById(R.id.registbutton);
        //backbutton = (ImageButton) findViewById(R.id.backbutton);
        setVerifyCode();
        recbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeGetRequest();
                rvfcddraw.setImageBitmap(pic);
            }
        });
        /*backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                RegisterActivity.this.finish();
            }
        });*/
        registbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePostRequest();
                if(result.equals("false"))
                {

                    Snackbar.make(v, feedback, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    result="";
                    feedback="";
                    user.setUsername("");
                    user.setInvitecode("");
                    user.setInvitor("");
                    user.setPassword("");
                    user.setSessionid("");
                    makeGetRequest();
                    rvfcddraw.setImageBitmap(pic);
                } else if (result.equals("true"))
                {
                    Snackbar.make(v, "regedit success", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    result="";
                    feedback="";
                    user.setUsername("");
                    user.setInvitecode("");
                    user.setInvitor("");
                    user.setPassword("");
                    user.setSessionid("");
                    //RegisterActivity.this.finish();
                }
            }
        });

    }
    private void setVerifyCode() {
        makeGetRequest();
        rvfcddraw.setImageBitmap(pic);
    }
    private void makeGetRequest() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet("http://"+Config.ip+"/deepnote/controller/admin/VerifycodeController.php");
                    // replace with your url
                    if (!session.equals("")) {
                        request.setHeader("Cookie", session);
                    }
                    HttpResponse response;
                    try {

                        response = client.execute(request);
                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        pic = BitmapFactory.decodeStream(is);
                        if (session.equals("")) {
                            Header it = response.getFirstHeader("Set-Cookie");
                            session = it.toString();
                            String[] heads = session.split(";");
                            String[] sessions = heads[0].split(":");
                            session = sessions[1];
                            user.setSessionid(session);
                        }
                        Log.d("PHPsession", session);

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
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void makePostRequest() {
        user.setUsername(ruserEdit.getText().toString());
        user.setPassword(rpswdEdit.getText().toString());
        user.setInvitor(invitor.getText().toString());
        user.setInvitecode(invitecode.getText().toString());
        verifycode = rvfcdEdit.getText().toString();
        if(user.getUsername().equals(""))
        {
            Snackbar.make(getCurrentFocus(), "用户名不得为空", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else
        {
            if(user.getUsername().length()>16||user.getUsername().length()<5)
            {
                Snackbar.make(getCurrentFocus(), "用户名必须介于5-16位之间", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else
            {
                if (user.getPassword().equals("")) {
                    Snackbar.make(getCurrentFocus(), "密码不得为空", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    if(user.getPassword().length()>16||user.getPassword().length()<6)
                    {
                        Snackbar.make(getCurrentFocus(), "密码必须介于6-16位之间", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        if (user.getInvitecode().equals("")||user.getInvitor().equals("")) {
                            Snackbar.make(getCurrentFocus(), "邀请者或邀请码不得为空", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        else
                        {
                            if (verifycode.equals("") | verifycode.length() < 4) {
                                Snackbar.make(getCurrentFocus(), "请检查您的验证码", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            } else {
                                user.setPassword(getMD5Str(rpswdEdit.getText().toString()));
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NameValuePair pair1 = new BasicNameValuePair("username", user.getUsername());
                                        NameValuePair pair2 = new BasicNameValuePair("password", user.getPassword());
                                        NameValuePair pair3 = new BasicNameValuePair("invitor",user.getInvitor());
                                        NameValuePair pair4=new BasicNameValuePair("invitecode",user.getInvitecode());
                                        NameValuePair pair5 = new BasicNameValuePair("verifycode", verifycode);
                                        List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                                        pairList.add(pair1);
                                        pairList.add(pair2);
                                        pairList.add(pair3);
                                        pairList.add(pair4);
                                        pairList.add(pair5);
                                        try {
                                            HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                                            HttpClient client = new DefaultHttpClient();
                                            HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/admin/RegeditController.php");
                                            request.setEntity(requestHttpEntity);
                                            String strResult;
                                            JSONObject jsonObject = null;
                                            // replace with your url
                                            if (!session.equals("")) {
                                                request.setHeader("Cookie", session);
                                            }
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
                                                    Toast.makeText(RegisterActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
                                });
                                thread.start();
                                try {
                                    thread.join();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public JSONObject getJSON(String sb) throws JSONException {
        return new JSONObject(sb);
    }
    private static String getMD5Str(String str)
    {
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e)
        {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++)
        {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }
}
