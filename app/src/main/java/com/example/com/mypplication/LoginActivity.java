package com.example.com.mypplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
/**********登录活动******/
public class LoginActivity extends AppCompatActivity {
    public  final static String SER_KEY = "com.example.com.mypplication";
    public static User user = new User();
    public static EditText luserEdit;
    public static EditText lpswdEdit;
    //public static EditText lvfcdEdit;
    //public static ImageView lvfcddraw;
    public static Button rebutton;
    public static ImageButton loginbutton;
    public static ImageButton gobutton;
    public static Bitmap pic;
    public static String session = new String("");
    public static String verifycode=new String("");
    public static String result=new String("");
    public static String feedback=new String("");
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
        Config.ip = "121.42.169.240";
        //Log.d("Config.ip", Config.ip);
        //put方法可以直接修改配置信息，不会重复添加
       //get出来的都是Object对象 如果是基本类型 需要用到封装类
        setContentView(R.layout.activity_login);
        prop = loadConfig(this, "/mnt/sdcard/config.properties");
        if (prop == null) {
//配置文件不存在的时候创建配置文件 初始化配置信息
            prop = new Properties();
            prop.put("ip",  Config.ip);
            prop.put("name","");
            prop.put("pwd","");
            saveConfig(this, "/mnt/sdcard/config.properties", prop);
        }
        else {
            prop = loadConfig(this, "/mnt/sdcard/config.properties");
            Config.ip = (String) prop.get("ip");
            String username=(String)prop.get("name");
            String pwd=(String)prop.get("pwd");
            if(!username.equals("")&&!pwd.equals(""))
            {
                user.setUsername(username);
                user.setPassword(pwd);
                makePostRequest1();
                if (result.equals("false")) {
                    result = "";
                    feedback = "";
                    user.setUsername("");
                    user.setInvitecode("");
                    user.setInvitor("");
                    user.setPassword("");
                    user.setSessionid("");
                    //makeGetRequest();
                    //lvfcddraw.setImageBitmap(pic);
                } else if (result.equals("true")) {
                    prop = loadConfig(LoginActivity.this, "/mnt/sdcard/config.properties");
                    if (prop == null) {
//配置文件不存在的时候创建配置文件 初始化配置信息
                        prop = new Properties();
                        prop.put("ip", Config.ip);
                        prop.put("name",user.getUsername());
                        prop.put("pwd",user.getPassword());
                        saveConfig(LoginActivity.this, "/mnt/sdcard/config.properties", prop);
                    }
                    Config.ip = (String) prop.get("ip");
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    mainIntent.putExtras(mBundle);
                    startActivity(mainIntent);
                    LoginActivity.this.finish();
                }
            }
        }
        //saveConfig(this, "/mnt/sdcard/config.properties", prop);
        luserEdit = (EditText) findViewById(R.id.luser);
        lpswdEdit = (EditText) findViewById(R.id.lpswd);
        //lvfcdEdit = (EditText) findViewById(R.id.lvfcde);
        //lvfcddraw = (ImageView) findViewById(R.id.lvfcdd);
        loginbutton = (ImageButton) findViewById(R.id.loginbutton);
        gobutton = (ImageButton) findViewById(R.id.gobutton);
        //makeGetRequest();
        //lvfcddraw.setImageBitmap(pic);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePostRequest();
                if (result.equals("false")) {
                    Snackbar.make(v, feedback, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    result = "";
                    feedback = "";
                    user.setUsername("");
                    user.setInvitecode("");
                    user.setInvitor("");
                    user.setPassword("");
                    user.setSessionid("");
                    //makeGetRequest();
                    //lvfcddraw.setImageBitmap(pic);
                } else if (result.equals("true")) {
                    Snackbar.make(v, "login success", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    prop = loadConfig(LoginActivity.this, "/mnt/sdcard/config.properties");
//配置文件不存在的时候创建配置文件 初始化配置信息
                        prop = new Properties();
                        prop.put("ip", Config.ip);
                        prop.put("name", user.getUsername());
                        prop.put("pwd", user.getPassword());
                        saveConfig(LoginActivity.this, "/mnt/sdcard/config.properties", prop);
                    Config.ip = (String) prop.get("ip");
                    MainActivity m=new MainActivity();
                    user.setActivity(m);
                    Intent mainIntent = new Intent(LoginActivity.this, LoadingActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    mainIntent.putExtras(mBundle);
                    startActivity(mainIntent);
                    result = "";
                    feedback = "";
                    user.setUsername("");
                    user.setInvitecode("");
                    user.setInvitor("");
                    user.setPassword("");
                    user.setSessionid("");
                    LoginActivity.this.finish();
                }
            }
        });
        /*rebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //makeGetRequest();
                //lvfcddraw.setImageBitmap(pic);
            }
        });*/
        gobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                //LoginActivity.this.finish();
            }
        });


    }

   /* private void makeGetRequest() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet("http://" + Config.ip + "/deepnote/controller/admin/VerifycodeController.php");
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
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void makePostRequest() {
            user.setUsername(luserEdit.getText().toString());
            user.setPassword(lpswdEdit.getText().toString());
        //verifycode = lvfcdEdit.getText().toString();
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

                            user.setPassword(getMD5Str(lpswdEdit.getText().toString()));
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    NameValuePair pair1 = new BasicNameValuePair("username", user.getUsername());
                                    NameValuePair pair2 = new BasicNameValuePair("password", user.getPassword());
                                    List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                                    pairList.add(pair1);
                                    pairList.add(pair2);
                                    try {
                                        HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                                        HttpClient client = new DefaultHttpClient();
                                        HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/admin/LoginController.php");
                                        request.setEntity(requestHttpEntity);
                                        String strResult;
                                        JSONObject jsonObject = null;
                                        // replace with your url

                                        HttpResponse response;
                                        try {
                                            response = client.execute(request);
                                            //Log.d("postresponse",response);
                                            if (response.getStatusLine().getStatusCode() == 200) {
                                                try {
                                                        Header it = response.getFirstHeader("Set-Cookie");
                                                        session = it.toString();
                                                        String[] heads = session.split(";");
                                                        String[] sessions = heads[0].split(":");
                                                        session = sessions[1];
                                                        user.setSessionid(session);
                                                    /**读取服务器返回过来的json字符串数据**/
                                                    strResult = EntityUtils.toString(response.getEntity());
                                                    jsonObject = getJSON(strResult);
                                                } catch (IllegalStateException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }catch (JSONException e1) {
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
                                                    if(result.equals("false"))
                                                    {
                                                        feedback=jsonObject.getString("feedback");
                                                        //Looper.prepare();
                                                        Log.d("loginfeedback",feedback);
                                                        //Toast.makeText(LoginActivity.this, feedback, Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {

                                                    }
                                                    //Toast.makeText(LoginActivity.this, "code:" + jsonObject.getString("code") + "name:" + names, Toast.LENGTH_SHORT).show();
                                                } catch (JSONException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }


                                            } else
                                                Toast.makeText(LoginActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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
    private void makePostRequest1() {
        //verifycode = lvfcdEdit.getText().toString();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                NameValuePair pair1 = new BasicNameValuePair("username", user.getUsername());
                                NameValuePair pair2 = new BasicNameValuePair("password", user.getPassword());
                                List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                                pairList.add(pair1);
                                pairList.add(pair2);
                                try {
                                    HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                                    HttpClient client = new DefaultHttpClient();
                                    HttpPost request = new HttpPost("http://"+Config.ip+"/deepnote/controller/admin/LoginController.php");
                                    request.setEntity(requestHttpEntity);
                                    String strResult;
                                    JSONObject jsonObject = null;
                                    // replace with your url

                                    HttpResponse response;
                                    try {
                                        response = client.execute(request);
                                        //Log.d("postresponse",response);
                                        if (response.getStatusLine().getStatusCode() == 200) {
                                            try {
                                                Header it = response.getFirstHeader("Set-Cookie");
                                                session = it.toString();
                                                String[] heads = session.split(";");
                                                String[] sessions = heads[0].split(":");
                                                session = sessions[1];
                                                user.setSessionid(session);
                                                /**读取服务器返回过来的json字符串数据**/
                                                strResult = EntityUtils.toString(response.getEntity());
                                                jsonObject = getJSON(strResult);
                                            } catch (IllegalStateException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }catch (JSONException e1) {
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
                                                if(result.equals("false"))
                                                {
                                                    feedback=jsonObject.getString("feedback");
                                                    //Looper.prepare();
                                                    Log.d("loginfeedback",feedback);
                                                    //Toast.makeText(LoginActivity.this, feedback, Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {

                                                }
                                                //Toast.makeText(LoginActivity.this, "code:" + jsonObject.getString("code") + "name:" + names, Toast.LENGTH_SHORT).show();
                                            } catch (JSONException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }


                                        } else
                                            Toast.makeText(LoginActivity.this, "POST提交失败", Toast.LENGTH_SHORT).show();
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