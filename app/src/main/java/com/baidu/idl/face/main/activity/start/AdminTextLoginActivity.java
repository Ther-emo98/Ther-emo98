package com.baidu.idl.face.main.activity.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.request;
import com.baidu.idl.facesdkdemo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AdminTextLoginActivity extends BaseActivity {
    EditText et_username;
    EditText et_password;
    Button btn_login;
    TextView textView;
    request requestUrl = new request();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_text_login);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
textView = findViewById(R.id.textView);
//        点击登录
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminTextLoginActivity.this, RegisterUserActivity.class));
//                Log.i("输入账号", "onClick: "+et_username.getText());
//                et_username.setText("admin");
//                et_password.setText("1234567");
//                try {
//                    StartLogin();
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }

//                登录成功后
//                startActivity(new Intent(AdminTextLoginActivity.this, RegisterUserActivity.class));
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminTextLoginActivity.this, RegisterUserActivity.class));
            }
        });
    }
//    登录
    public void StartLogin() throws JSONException {

        //      实例化OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient();

        String jsonStringData = "";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", et_username.getText());
        jsonObject.put("password", et_password.getText());
        jsonStringData = jsonObject.toString();
        RequestBody body = RequestBody.create(jsonStringData, MediaType.parse("application/json"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //      构建request请求
                Request request = new Request.Builder()
                        .post(body)
                        .url(requestUrl.ApiUrl+"/login")
                        .build();
//      发送请求获取返回数据
                Call call = okHttpClient.newCall(request);
//      获取响应体
                ResponseBody body = null;
                String string = null;
                // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String resp = response.body().string();
                        Log.i("登录成功", "resp: "+resp);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(resp);
                                    int code = jsonObject.getInt("code");
                                    String token = jsonObject.getString("token");
                                    if(code==200){
                                        Log.i("登录成功", "token: "+token);
//                                        保存token
                                        //步骤1：创建一个SharedPreferences对象
                                        SharedPreferences sharedPreferences= getSharedPreferences("Token",MODE_PRIVATE);
                                        //步骤2： 实例化SharedPreferences.Editor对象
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        //步骤3：将获取过来的值放入文件
                                        editor.putString("token",token);
                                        //步骤4：提交
                                       boolean Iscommit= editor.commit();
                                        Log.i("缓存成功", "Iscommit: "+Iscommit);
                                        Toast.makeText(AdminTextLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                        //                登录成功后
                                        startActivity(new Intent(AdminTextLoginActivity.this, RegisterUserActivity.class));
                                    }else {
                                        Toast.makeText(AdminTextLoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, IOException e) { // 请求失败
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdminTextLoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }
}
