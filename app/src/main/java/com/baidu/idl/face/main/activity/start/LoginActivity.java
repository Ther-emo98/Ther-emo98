package com.baidu.idl.face.main.activity.start;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.attribute.utils.AttributeConfigUtils;
import com.baidu.idl.face.main.drivermonitor.utils.DriverMonitorConfigUtils;
import com.baidu.idl.face.main.finance.utils.FinanceConfigUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceDepthAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceNIRAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceRGBAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.attendance.FaceRGBNirDepthAttendanceActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.AttendanceConfigUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.RegisterConfigUtils;
import com.baidu.idl.main.facesdk.gazelibrary.utils.GazeConfigUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.IdentifyConfigUtils;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.PaymentConfigUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewRgbNirDepthActivity;
import com.baidu.idl.main.facesdk.utils.DensityUtils;
import com.baidu.idl.main.facesdk.utils.GateConfigUtils;
import com.baidu.idl.main.facesdk.utils.StreamUtil;
import com.baidu.idl.main.facesdk.utils.ToastUtils;
import com.baidu.idl.main.facesdk.view.PreviewTexture;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends BaseActivity {
    RelativeLayout adminlogin;
    RelativeLayout employeeLogin;
    RelativeLayout adminTextLogin;
    @SuppressLint("MissingInflatedId")
    private Context mContext;
    private Future future;
    private Handler mHandler = new Handler();
    private PopupWindow popupWindow;
    private View view1;
    private RelativeLayout layout_home;
    private RelativeLayout home_personRl;
    private int mLiveType;
    private PopupWindow mPopupMenu;
    private PopupWindow mPopupMenuFirst;
    private ImageView home_menuImg;
    private boolean isCheck = false;
    private TextView home_dataTv;

    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;
    private PreviewTexture[] previewTextures;
    private Camera[] mCamera;
    private TextureView checkRBGTexture;
    private TextureView checkNIRTexture;
    private ProgressBar progressBar;
    private TextView progressText;
    private View progressGroup;
    private boolean isDBLoad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mContext = this;
        adminlogin = findViewById(R.id.adminLogin);
        employeeLogin = findViewById(R.id.employeeLogin);
        adminTextLogin = findViewById(R.id.adminTextLogin);
//        注册点击事件
        adminlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                点击管理员登录
                Log.i("Admin", "onClick: 点击了管理员登录");
//                调用人脸识别接口
                judgeLiveType(0,
                        FaceRGBAttendanceActivity.class,
                        FaceNIRAttendanceActivity.class,
                        FaceDepthAttendanceActivity.class,
                        FaceRGBNirDepthAttendanceActivity.class,1);
            }
        });
        employeeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                点击员工登录
                Log.i("employee", "onClick: 点击了员工登录");
                com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager.getInstance().setActiveLog();
                judgeLiveType(0,
                        FaceRGBAttendanceActivity.class,
                        FaceNIRAttendanceActivity.class,
                        FaceDepthAttendanceActivity.class,
                        FaceRGBNirDepthAttendanceActivity.class,0);
            }
        });
        adminTextLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                点击管理员账号密码登录
                Log.i("adminText", "onClick: 点击了管理员账号密码登录");
                startActivity(new Intent(mContext, AdminTextLoginActivity.class));

            }
        });
        initRGBCheck();
        initDBApi();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //这个1000就是要和我们跳转时传入的值要相等
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            boolean strReturn = data.getBooleanExtra("isGo",false);
            int IsAdmin = data.getIntExtra("IsAdmin",0);
            String UserMsg = data.getStringExtra("UserMsg");
            Log.e("isGo","返回的数据："+strReturn);
            if(IsAdmin!=1){
                    alertDialog1(strReturn,IsAdmin,UserMsg);
            }else {
                //跳转管理员
                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
            }


        }
    }

    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls, Class<?> rndCls,int IsAdmin) {
        switch (type) {
            case 0: { // 不使用活体
                Intent intent = new Intent(LoginActivity.this,rgbCls);
                //在Intent对象当中添加一个键值对
                //key要记住，接收的时候要靠key来接收
                intent.putExtra("IsAdmin",IsAdmin);
                startActivityForResult(intent,1000);
//                startActivity(new Intent(LoginActivity.this, rgbCls));
                break;
            }

            case 1: { // RGB活体
                startActivity(new Intent(LoginActivity.this, rgbCls));
                break;
            }

            case 2: { // NIR活体
                startActivity(new Intent(LoginActivity.this, nirCls));
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
                startActivity(new Intent(LoginActivity.this, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(LoginActivity.this, depthCls));
                break;
            }

            case 6: { // Pico
                //  startActivity(new Intent(HomeActivity.this,
                // PicoFaceDepthLivenessActivity.class));
                break;
            }

            default:
                startActivity(new Intent(LoginActivity.this, depthCls));
                break;
        }
    }
    public void alertDialog1(boolean strReturn,int IsAdmin,String UserMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle("是否进入资产领用界面?")
                .setMessage("请点击确认按钮进入！")
                .setCancelable(false)
                .setNeutralButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("点击", "onClick: 正在进入领用介面");

                        GoToGoodsLingYong(strReturn,IsAdmin,UserMsg);
                    }
                }).create();
        builder.show();
    }
    public void GoToGoodsLingYong(boolean strReturn,int IsAdmin,String UserMsg){
        if(strReturn){
//                跳转
            if(IsAdmin==1){
//跳转管理员
                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
            }else {
//                    进入领用
                Intent intent = new Intent(LoginActivity.this,GoodsLingYongActivity.class);
                intent.putExtra("UserMsg",UserMsg);
                startActivity(intent);
            }
        }
    }

    private void initDBApi(){
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        isDBLoad = false;
        future = Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                FaceApi.getInstance().init(new DBLoadListener() {

                    @Override
                    public void onStart(int successCount) {
                        Log.i("进入了初始化人脸api", "onStart: 进入了初始化人脸api");
                        if (successCount < 5000 && successCount != 0){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    loadProgress(10);
                                }
                            });
                        }
                    }

                    @Override
                    public void onLoad(final int finishCount, final int successCount, final float progress) {
                        if (successCount > 5000 || successCount == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress((int) (progress * 100));
                                    progressText.setText(((int) (progress * 100)) + "%");
                                }
                            });
                        }
                    }

                    @Override
                    public void onComplete(final List<User> users , final int successCount) {
//                        FileUtils.saveDBList(HomeActivity.this, users);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FaceApi.getInstance().setUsers(users);
                                if (successCount > 5000 || successCount == 0) {
//                                    progressGroup.setVisibility(View.GONE);
                                    isDBLoad = true;
                                }
                            }
                        });
                    }

                    @Override
                    public void onFail(final int finishCount, final int successCount, final List<User> users) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FaceApi.getInstance().setUsers(users);
//                                progressGroup.setVisibility(View.GONE);
                                ToastUtils.toast(LoginActivity.this,
                                        "人脸库加载失败,共" + successCount + "条数据, 已加载" + finishCount + "条数据");
                                isDBLoad = true;
                            }
                        });
                    }
                }, mContext);
            }
        });
    }

    private void loadProgress(float i){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress((int) ((i / 5000f) * 100));
                progressText.setText(((int) ((i / 5000f) * 100)) + "%");
                if (i < 5000){
                    loadProgress(i + 100);
                }else {
                    progressGroup.setVisibility(View.GONE);
                    isDBLoad = true;
                }
            }
        },10);
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release(0);
        release(1);

        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        FaceApi.getInstance().cleanRecords();
    }

    private void initRGBCheck(){
        if (isSetCameraId()){
            return;
        }
        int mCameraNum = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
        }
        if (mCameraNum > 1){
            try {
                mCamera = new Camera[mCameraNum];
                previewTextures = new PreviewTexture[mCameraNum];
                mCamera[0] = Camera.open(0);
                previewTextures[0] = new PreviewTexture(this, checkRBGTexture);
                previewTextures[0].setCamera(mCamera[0], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1){
                            setRgbCameraId(0);
                        }
                        release(0);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                mCamera[1] = Camera.open(1);
                previewTextures[1] = new PreviewTexture(this, checkNIRTexture);
                previewTextures[1].setCamera(mCamera[1], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        int check = StreamUtil.checkNirRgb(data, PREFER_WIDTH, PREFER_HEIGHT);
                        if (check == 1){
                            setRgbCameraId(1);
                        }
                        release(1);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            setRgbCameraId(0);
        }
    }

    private void setRgbCameraId(int index){
        SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.face.main.finance.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.face.main.attribute.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.face.main.drivermonitor.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.gazelibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.paymentlibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);
        com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig.getBaseConfig().setRBGCameraId(index);

        AttendanceConfigUtils.modityJson();
        AttributeConfigUtils.modityJson();
        DriverMonitorConfigUtils.modityJson();
        FinanceConfigUtils.modityJson();
        GateConfigUtils.modityJson();
        GazeConfigUtils.modityJson();
        IdentifyConfigUtils.modityJson();
        PaymentConfigUtils.modityJson();
        RegisterConfigUtils.modityJson();

    }
    private boolean isSetCameraId(){
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.attendancelibrary.
                        model.SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.face.main.finance.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.face.main.attribute.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.face.main.drivermonitor.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.gazelibrary.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.paymentlibrary.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.identifylibrary.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1 ||
                com.baidu.idl.main.facesdk.registerlibrary.user.model.
                        SingleBaseConfig.getBaseConfig().getRBGCameraId() == -1){
            return false;
        }else {
            return true;
        }
    }

    private void release(int id){
        if (mCamera != null && mCamera[id] != null) {
            if (mCamera[id] != null) {
                mCamera[id].setPreviewCallback(null);
                mCamera[id].stopPreview();
                previewTextures[id].release();
                mCamera[id].release();
                mCamera[id] = null;
            }
        }
    }

}
