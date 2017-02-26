package com.example.com.mypplication;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
/***********检测网络选择入口活动*****************/
public class SelectLauchActivity extends AppCompatActivity {
    private Properties prop;
    private  String isc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_);
        prop = loadConfig(this, "/mnt/sdcard/config.properties");
        if (prop == null) {
//配置文件不存在的时候创建配置文件 初始化配置信息
            prop = new Properties();
            prop.put("ip", "223.3.77.70");
            saveConfig(this, "/mnt/sdcard/config.properties", prop);
        }
        Config.ip = (String) prop.get("ip");
        saveConfig(this, "/mnt/sdcard/config.properties", prop);
        testandset("223.3.77.70");
    }
    public void  testandset(String str)
    {
        isc=pingHost(str);
        if (isc.equals("s"))
        {
            Intent LauchIntent=new Intent(SelectLauchActivity.this, LoginActivity.class);
            startActivity(LauchIntent);
            SelectLauchActivity.this.finish();
        }
        if(isc.equals("f"))
        {
            final EditText editText2 = new EditText(SelectLauchActivity.this);
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(SelectLauchActivity.this);
            builder2.setTitle("ip地址错误");
            builder2.setMessage("请输入ip:");
            builder2.setView(editText2);
            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prop.clear();
                    prop.put("ip", editText2.getText().toString());
                    saveConfig(SelectLauchActivity.this, "/mnt/sdcard/config.properties", prop);
                    Config.ip = (String) prop.get("ip");
                    if (editText2.getText().toString().equals("")) {
                          testandset("223.3.77.70");
                    } else {
                        testandset(Config.ip);
                    }
                }
            });
            builder2.show();
        }
    }
    public static String pingHost(String str){
        String resault="";
            List<String>ss=CommandUtil.execute("ping -c 1 -w 3 " + str);
        Log.d("ssssss", ss.toString());
        if (ss.get(3).contains("100%")||ss.size()==0) {
            return "f";
        }
        else {
            return "s";
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
    private void makeGetRequest() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet("http://" + Config.ip + "/deepnote/controller/admin/VerifycodeController.php");
                    // replace with your url
                    HttpResponse response;
                    try {

                        response = client.execute(request);
                        if(response.getStatusLine().getStatusCode()==200) {
                            Intent LauchIntent=new Intent(SelectLauchActivity.this, LoginActivity.class);
                            startActivity(LauchIntent);
                            SelectLauchActivity.this.finish();
                        }else
                        {
                            final EditText editText2 = new EditText(SelectLauchActivity.this);
                            final AlertDialog.Builder builder2 = new AlertDialog.Builder(SelectLauchActivity.this);
                            builder2.setTitle("ip地址错误");
                            builder2.setMessage("请输入ip:");
                            builder2.setView(editText2);
                            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prop.clear();
                                    prop.put("ip", editText2.getText().toString());
                                    saveConfig(SelectLauchActivity.this, "/mnt/sdcard/config.properties", prop);
                                    Config.ip = (String) prop.get("ip");
                                    makeGetRequest();
                                }
                            });
                            builder2.show();
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Log.d("wrong:", "wrong");
                        isc=null;
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
        if(isc==null)
        {
            final EditText editText2 = new EditText(SelectLauchActivity.this);
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(SelectLauchActivity.this);
            builder2.setTitle("ip地址错误");
            builder2.setMessage("请输入ip:");
            builder2.setView(editText2);
            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prop.clear();
                    prop.put("ip", editText2.getText().toString());
                    saveConfig(SelectLauchActivity.this, "/mnt/sdcard/config.properties", prop);
                    Config.ip = (String) prop.get("ip");
                    makeGetRequest();
                }
            });
            builder2.show();
        }
    }
}
