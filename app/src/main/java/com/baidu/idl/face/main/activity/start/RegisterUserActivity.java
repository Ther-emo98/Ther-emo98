package com.baidu.idl.face.main.activity.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.FaceUserAdapter;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.FaceUser;
import com.baidu.idl.face.main.utils.request;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.activity.UserManagerActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewRgbNirDepthActivity;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegisterUserActivity extends BaseActivity {
    //第一步：定义对象
    ListView listView;
//    页吗
    int page = 1;
    request requestUrl = new request();
    //第三步：准备数据
    List<FaceUser> fruitlist = new ArrayList<>();
    FaceUserAdapter adapter;
    boolean IsLoad = true;

    Button facemangner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);
        //第二步：绑定控件
        listView = (ListView) findViewById(R.id.registerlistView);
        facemangner = findViewById(R.id.face_btn);
        ////                                        定义适配器 控件 -桥梁-数据
        adapter=new FaceUserAdapter(RegisterUserActivity.this,R.layout.faceuser_item,fruitlist);
        listView.setAdapter(adapter);
//        获取用户列表
//        try {
//            GetUserList();
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }

//        进入人脸库
        facemangner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUserActivity.this, UserManagerActivity.class));
            }
        });


//        监听点击哪位用户
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("选择得数据", "onItemClick: 选择得id"+fruitlist.get(position).getId());
//                开始注册人脸
                com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().setActiveLog();

                judgeLiveType(0, FaceRegisterNewActivity.class, FaceRegisterNewNIRActivity.class,
                        FaceRegisterNewDepthActivity.class, FaceRegisterNewRgbNirDepthActivity.class,fruitlist.get(position).getId(),requestUrl.ApiUrl,fruitlist.get(position).getName());

            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 滚动状态改变时的回调，可以不处理
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 当前可见的第一个列表项的位置
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                // 判断是否滚动到底部
                if(lastVisibleItem!=0&&totalItemCount!=0){
                    if(lastVisibleItem==totalItemCount){
                        // 加载下一页的数据
                        if (IsLoad){

                            try {
                                page++;
                                GetUserList();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
//        获取员工信息
        page = 1;
        IsLoad= true;
        fruitlist.clear();
        try {
            GetUserList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls, Class<?> rndCls, int id, String url, String userName) {
        switch (type) {
            case 0: { // 不使用活体
                Intent intent = new Intent(RegisterUserActivity.this,rgbCls);
                //在Intent对象当中添加一个键值对
                //key要记住，接收的时候要靠key来接收
                intent.putExtra("faceid",id);
                intent.putExtra("url",url);
                intent.putExtra("userName",userName);
                startActivity(intent);
//                startActivity(new Intent(RegisterUserActivity.this, rgbCls));
                break;
            }

            case 1: { // RGB活体
                Intent intent = new Intent(RegisterUserActivity.this,rgbCls);
                //在Intent对象当中添加一个键值对
                //key要记住，接收的时候要靠key来接收
                intent.putExtra("faceid",id);
                intent.putExtra("url",url);
                intent.putExtra("userName",userName);
                startActivity(intent);
//                startActivity(new Intent(RegisterUserActivity.this, rgbCls));
                break;
            }

            case 2: { // NIR活体
                Intent intent = new Intent(RegisterUserActivity.this,nirCls);
                //在Intent对象当中添加一个键值对
                //key要记住，接收的时候要靠key来接收
                intent.putExtra("faceid",id);
                intent.putExtra("url",url);
                intent.putExtra("userName",userName);
                startActivity(intent);
//                startActivity(new Intent(RegisterUserActivity.this, nirCls));
                break;
            }

            case 3: { // 深度活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, depthCls);
                break;
            }

            case 4: { // rgb+nir+depth活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, rndCls);
            }
        }

    }
    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(RegisterUserActivity.this, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(RegisterUserActivity.this, depthCls));
                break;
            }

            case 6: { // Pico
                //  startActivity(new Intent(HomeActivity.this,
                // PicoFaceDepthLivenessActivity.class));
                break;
            }

            default:
                startActivity(new Intent(RegisterUserActivity.this, depthCls));
                break;
        }
    }
//获取用户列表
    public void GetUserList() throws JSONException {
        //      实例化OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient();
        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= getSharedPreferences("Token", MODE_PRIVATE);
        String token=sharedPreferences.getString("token","");
        Log.i("page", "页吗page: "+page);
//        String jsonStringData = "";
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("pageNum", page);
//        jsonObject.put("pageSize", "8");
//        jsonObject.put("reasonable", false);
//        jsonStringData = jsonObject.toString();
//        RequestBody body = RequestBody.create(jsonStringData, MediaType.parse("application/json"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //      构建request请求
                Request request = new Request.Builder()
                        .get()
                        .url(requestUrl.ApiUrl+"/system/user/list?pageNum="+page+"&pageSize=8&reasonable=false")
                        .header("Authorization","Bearer "+token)
                        .build();
//      发送请求获取返回数据
                Call call = okHttpClient.newCall(request);
                // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String resp = response.body().string();
                        Log.i("查询成功", "run: "+resp);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(resp);
                                    int code = jsonObject.getInt("code");
                                    JSONArray rows = jsonObject.getJSONArray("rows");
                                    Log.i("rows.length()数据个数", "run: "+rows.length());
                                    Log.i("状态码", "run: "+code);
                                    if(code==200){
                                        if(rows.length()==0){
                                            IsLoad = false;
                                            return;
                                        }
                                     for (int i = 0; i <rows.length() ; i++) {
                                    FaceUser pineapple=new FaceUser(rows.getJSONObject(i).getString("userName"), rows.getJSONObject(i).getString("phonenumber"),rows.getJSONObject(i).getString("isRegisterFace"),rows.getJSONObject(i).getInt("userId"));
                                     fruitlist.add(pineapple);
                                     }
                                        adapter.notifyDataSetChanged();
                                    }else {
                                        IsLoad = false;
                                        Toast.makeText(RegisterUserActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(RegisterUserActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }
}
