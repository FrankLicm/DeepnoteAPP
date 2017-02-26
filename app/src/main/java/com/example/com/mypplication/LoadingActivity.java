package com.example.com.mypplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class LoadingActivity extends Activity {
    User user;
    public  final static String SER_KEY = "com.example.com.mypplication";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra(SER_KEY);
        setContentView(R.layout.loading);
        if(user.getActivity().getClass()==NoteChartActivity.class) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //等待10000毫秒后销毁此页面，并提示登陆成功
                    LoadingActivity.this.finish();
                    Toast.makeText(getApplicationContext(), "加载成功", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(LoadingActivity.this, user.getActivity().getClass());
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(SER_KEY, user);
                    mainIntent.putExtras(mBundle);
                    startActivity(mainIntent);
                }
            }, 1000);
        }else
        {
            if (user.getActivity().getClass()==MainActivity.class&&user.getMainflag()==1) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //等待10000毫秒后销毁此页面，并提示登陆成功
                        LoadingActivity.this.finish();
                        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(LoadingActivity.this, user.getActivity().getClass());
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable(SER_KEY, user);
                        mainIntent.putExtras(mBundle);
                        startActivity(mainIntent);
                    }
                }, 1000);
                user.setMainflag(0);
            }else if(user.getActivity().getClass()==MainActivity.class&&user.getMainflag()==0)
            {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //等待10000毫秒后销毁此页面，并提示登陆成功
                        LoadingActivity.this.finish();
                        Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(LoadingActivity.this, user.getActivity().getClass());
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable(SER_KEY, user);
                        mainIntent.putExtras(mBundle);
                        startActivity(mainIntent);
                    }
                }, 1000);
            }else
            {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //等待10000毫秒后销毁此页面，并提示登陆成功
                        LoadingActivity.this.finish();
                        Toast.makeText(getApplicationContext(), "加载成功", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(LoadingActivity.this, user.getActivity().getClass());
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable(SER_KEY, user);
                        mainIntent.putExtras(mBundle);
                        startActivity(mainIntent);
                    }
                }, 1000);
            }
        }
    }
}
