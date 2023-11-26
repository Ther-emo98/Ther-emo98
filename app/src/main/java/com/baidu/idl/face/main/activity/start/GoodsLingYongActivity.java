package com.baidu.idl.face.main.activity.start;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.GoodsAdapter;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.Goods;
import com.baidu.idl.face.main.utils.request;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.attendancelibrary.SubModuleInterface;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTagEpcLog;
import com.gg.reader.api.dal.HandlerTagEpcOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBaseEpcInfo;
import com.gg.reader.api.protocol.gx.LogBaseEpcOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseStop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoodsLingYongActivity extends BaseActivity implements SubModuleInterface {
    Button submit_btn;
//    用户名称
    TextView username;
//    用户手机号
    TextView userphone;
//    用户身份
    TextView userstanding;
    String UserMsg ="";
//    json化个人信息
    JSONObject jsonObjectUserMsg;
    ListView listView;
    request requestUrl = new request();
    List<Goods> GoodsList = new ArrayList<>();
    GoodsAdapter adapter;
    Button clearbtn;
//    EPC列表
HashSet<String> EpcList = new HashSet<String>();

    GClient client = new GClient();
//    是否正在读取
    boolean IsRead = false;
    private AlertDialog alertDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goodslingyong);
        Intent intent = getIntent();
        UserMsg = intent.getStringExtra("UserMsg");
        Log.i("登录的个人信息", "onCreate: 传递的参数:"+UserMsg);

        submit_btn = findViewById(R.id.submit_btn);
        username = findViewById(R.id.username);
        userphone = findViewById(R.id.userphone);
        userstanding = findViewById(R.id.userstanding);
        clearbtn = findViewById(R.id.clear_btn);
        //第二步：绑定控件
        listView = (ListView) findViewById(R.id.lingyonglistView);
        ////                                        定义适配器 控件 -桥梁-数据
        adapter=new GoodsAdapter(GoodsLingYongActivity.this,R.layout.goods_item,GoodsList);
        listView.setAdapter(adapter);
        try {
            jsonObjectUserMsg = new JSONObject(UserMsg);
            //            赋值文本框
            username.setText(jsonObjectUserMsg.getString("userName"));
            userphone.setText(jsonObjectUserMsg.getString("phonenumber"));
            if(jsonObjectUserMsg.getBoolean("admin")){
                userstanding.setText("管理员");
            }else {
                userstanding.setText("普通角色");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        清空按钮
        clearbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                GoodsList.clear();
                EpcList.clear();
                adapter.notifyDataSetChanged();

            }
        });
//        提交按钮
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                点击提交领用数据
                if(GoodsList.isEmpty()){
                    Toast.makeText(GoodsLingYongActivity.this, "请读取需要领用的资产", Toast.LENGTH_SHORT).show();
                    return;
                }else {
//                    调用领用
                    if(IsRead){
//                    关闭读取
                        StopRead();
                    }
                    //                        上传领用数据
                    try {
                        UploadLingYongData();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                initRfid();
            }
        }).start();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    // 实现子模块跳转接口的方法
    @Override
    public void navigateToMainModule(Context context) {
        Intent intent = new Intent(context, GoodsLingYongActivity.class);
        startActivity(intent);
    }
    public void initRfid(){
        boolean IsConnect = client.openAndroidSerial("/dev/ttyS3:115200",1000);
        if(IsConnect){
//                          Toast.makeText(GoodsLingYongActivity.this, "RFID初始化成功!", Toast.LENGTH_SHORT).show();
            //           自动调用RFID读取
            // 订阅标签上报事件
            client.onTagEpcLog = new HandlerTagEpcLog() {
                @Override
                public void log(String readName, LogBaseEpcInfo logBaseEpcInfo) {
// 回调内部如有阻塞，会影响 API 正常使用
// 标签回调数量较多，请将标签数据先缓存起来再作业务处理
                    if (null != logBaseEpcInfo && 0 == logBaseEpcInfo.getResult()) {
                        System.out.println(logBaseEpcInfo.getEpc());
                        if(!EpcList.contains(logBaseEpcInfo.getEpc())){
                            Log.i("EPC", "log: EPC"+logBaseEpcInfo.getEpc());
                            EpcList.add(logBaseEpcInfo.getEpc());
                            //                       获取资产信息
                            GetGoodsMsg(logBaseEpcInfo.getEpc());
                        }
                    }
                }
            };
            client.onTagEpcOver = new HandlerTagEpcOver() {
                @Override
                public void log(String s, LogBaseEpcOver logBaseEpcOver) {
                    System.out.println("Epc log over.");
                }
            };
            //           开始读取
            Read();
        }else {
            Toast.makeText(GoodsLingYongActivity.this, "RFID初始化失败,请重启APP", Toast.LENGTH_SHORT).show();
        }


    }
    public void Read(){
        MsgBaseInventoryEpc msgBaseInventoryEpc = new MsgBaseInventoryEpc();
        msgBaseInventoryEpc.setAntennaEnable(EnumG.AntennaNo_1 | EnumG.AntennaNo_2 |
                EnumG.AntennaNo_3 | EnumG.AntennaNo_4);
        msgBaseInventoryEpc.setInventoryMode(EnumG.InventoryMode_Inventory);
        client.sendSynMsg(msgBaseInventoryEpc);
        if (0 == msgBaseInventoryEpc.getRtCode()) {
            System.out.println("Inventory epc successful.");
            IsRead = true;
        } else {
            System.out.println("Inventory epc error.");
        }
    }
    public void StopRead(){
        MsgBaseStop msgBaseStop = new MsgBaseStop();
        client.sendSynMsg(msgBaseStop);
        IsRead = false;
        if (0 == msgBaseStop.getRtCode()) {
            System.out.println("Stop successful.");
            IsRead = false;
        } else {
            System.out.println("Stop error.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        停止读取
        StopRead();
//        关闭rfid
        client.close();
    }
    //                        上传领用数据
   public void UploadLingYongData() throws JSONException {

       SharedPreferences sharedPreferences= getSharedPreferences("ApiUrl", MODE_PRIVATE);
       final String url=sharedPreferences.getString("apiUrl","");
       //      实例化OkHttpClient
       final OkHttpClient okHttpClient = new OkHttpClient();
       //步骤1：创建一个SharedPreferences对象
       SharedPreferences sharedPreferencess= getSharedPreferences("Token", MODE_PRIVATE);
       final String token=sharedPreferencess.getString("token","");
       showLoadingDialog();
       new Thread(new Runnable() {
           @Override
           public void run() {
               String jsonStringData = "";
               JSONObject jsonObject = new JSONObject();
               JSONArray jsonArrayData = new JSONArray();
               try {
                   //for in用法
                   for(Goods item:GoodsList){
                           JSONObject obj = new JSONObject();
                           obj.put("productId",item.getId());
                           obj.put("usageStatus","已领用");
                           jsonArrayData.put(obj);
                   }
                   jsonObject.put("assetUsageNumber", generateOrderNumber());
                   jsonObject.put("totalQuantity", GoodsList.size());
                   jsonObject.put("infoList", jsonArrayData);
               } catch (JSONException e) {
                   throw new RuntimeException(e);
               }
               jsonStringData = jsonObject.toString();
               RequestBody body = RequestBody.create(jsonStringData, MediaType.parse("application/json"));
               //      构建request请求
               Request request = new Request.Builder()
                       .post(body)
                       .url(url+"/system/usage")
                       .header("Authorization","Bearer "+token)
                       .build();
//      发送请求获取返回数据
               Call call = okHttpClient.newCall(request);
               // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
               call.enqueue(new Callback() {

                   @Override
                   public void onResponse(Call call, Response response) throws IOException {
                       final String resp = response.body().string();
                       Log.i("领用成功", "run: "+resp);
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               try {

                                   JSONObject jsonObject = new JSONObject(resp);
                                   int code = jsonObject.getInt("code");

                                   Log.i("状态码", "run: "+code);
                                   if(code==200){
//                                       Toast.makeText(GoodsLingYongActivity.this, "领用成功", Toast.LENGTH_SHORT).show();
                                       alertDialog1("领用成功!");

//                                       重启读取
                                       Read();
                                   }else {
                                       alertDialog1("领用失败!");
//                                       Toast.makeText(GoodsLingYongActivity.this, "领用失败", Toast.LENGTH_SHORT).show();
                                   }
                                   dismissLoadingDialog();
                                   adapter.notifyDataSetChanged();
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
                               dismissLoadingDialog();
                               alertDialog1("领用失败!");
//                               Toast.makeText(GoodsLingYongActivity.this, "领用失败", Toast.LENGTH_SHORT).show();
                           }
                       });
                   }
               });
           }
       }).start();
    }
    //                       获取资产信息
    public void GetGoodsMsg(String epc){
        SharedPreferences sharedPreferences= getSharedPreferences("ApiUrl", MODE_PRIVATE);
        final String url=sharedPreferences.getString("apiUrl","");
        //      实例化OkHttpClient
        final OkHttpClient okHttpClient = new OkHttpClient();
        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferencess= getSharedPreferences("Token", MODE_PRIVATE);
        final String token=sharedPreferencess.getString("token","");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //      构建request请求
                Request request = new Request.Builder()
                        .get()
                        .url(url+"/system/product/getInfo?rfidNumber="+epc)
                        .header("Authorization","Bearer "+token)
                        .build();
//      发送请求获取返回数据
                Call call = okHttpClient.newCall(request);
                // 加入HTTP请求队列。异步调用，并设置接口应答的回调方法
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String resp = response.body().string();
                        Log.i("查询成功", "run: "+resp);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(resp);
                                    int code = jsonObject.getInt("code");

                                    Log.i("状态码", "run: "+code);
                                    if(code==200){
                                        if(jsonObject.getJSONArray("rows").length()>0){
                                            JSONArray rows = jsonObject.getJSONArray("rows");
                                            Goods pineapple=new Goods(rows.getJSONObject(0).getString("productNumber"),rows.getJSONObject(0).getString("productName"),rows.getJSONObject(0).getString("rfidNumber"),rows.getJSONObject(0).getString("warehouseIdNmae"),rows.getJSONObject(0).getString("productTypeIdName"),rows.getJSONObject(0).getInt("id"));
                                            GoodsList.add(pineapple);
                                        }
                                    }else {
                                        Toast.makeText(GoodsLingYongActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                                    }
                                    adapter.notifyDataSetChanged();
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
                                Toast.makeText(GoodsLingYongActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).start();
    }
    public static String generateOrderNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());
        return timestamp;
    }
    /**
     * 展示加载中窗口
     */
    public void showLoadingDialog() {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });
        alertDialog.show();
        alertDialog.setContentView(R.layout.layout_loading_dialog);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 在fragment里调用这个，关闭加载中弹窗
     * 关闭
     */
    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
    public void alertDialog1(String Msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle(""+Msg)
                .setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Msg.equals("领用成功!")){
                            GoodsList.clear();
                            EpcList.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }})
                .setNeutralButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Msg.equals("领用成功!")){
                            finish();
                        }
                    }
                }).create();
        if(Msg.equals("领用成功!")){
            builder.setMessage("是否退出页面？");
        }else {
            builder.setMessage("领用失败!");
        }
        builder.show();
    }

}
