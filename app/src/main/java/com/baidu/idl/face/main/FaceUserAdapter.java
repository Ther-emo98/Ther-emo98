package com.baidu.idl.face.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baidu.idl.face.main.model.FaceUser;
import com.baidu.idl.facesdkdemo.R;

import java.util.Collections;
import java.util.List;

public class FaceUserAdapter extends ArrayAdapter<FaceUser> {
    public FaceUserAdapter(@NonNull Context context, int resource, @NonNull List<FaceUser> objects) {
        super(context, resource, objects);
    }
    //每个子项被滚动到屏幕内的时候会被调用
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FaceUser fruit=getItem(position);//得到当前项的 Fruit 实例
        //为每一个子项加载设定的布局
        View view= LayoutInflater.from(getContext()).inflate(R.layout.faceuser_item,parent,false);
        //分别获取 image view 和 textview 的实例
        TextView faceusername =view.findViewById(R.id.faceuser_name);
        TextView faceuserphone =view.findViewById(R.id.faceuser_phone);
        TextView faceuserisface=view.findViewById(R.id.faceuser_isface);
        // 设置要显示的图片和文字
        faceusername.setText(fruit.getName());
        faceuserphone.setText(fruit.getPhone());
        faceuserisface.setText(fruit.getIsFace());
        return view;
    }

}
