package com.baidu.idl.face.main;
        import android.content.Context;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;
        import com.baidu.idl.face.main.model.Goods;
        import com.baidu.idl.facesdkdemo.R;
        import java.util.List;

public class GoodsAdapter extends ArrayAdapter<Goods> {
    public GoodsAdapter(@NonNull Context context, int resource, @NonNull List<Goods> objects) {
        super(context, resource, objects);
    }
    //每个子项被滚动到屏幕内的时候会被调用
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Goods fruit=getItem(position);//得到当前项的 Fruit 实例
        //为每一个子项加载设定的布局
        View view= LayoutInflater.from(getContext()).inflate(R.layout.goods_item,parent,false);
        //分别获取 image view 和 textview 的实例
        TextView product_number =view.findViewById(R.id.product_number);
        TextView productType_name =view.findViewById(R.id.productType_name);
        TextView warehouse_name=view.findViewById(R.id.warehouse_name);
        TextView rfid_number=view.findViewById(R.id.rfid_number);
        TextView product_name=view.findViewById(R.id.product_name);
        // 设置要显示的图片和文字
        product_number.setText(fruit.getProductNumber());
        productType_name.setText(fruit.getProductTypeIdName());
        warehouse_name.setText(fruit.getwarehouseIdName());
        rfid_number.setText(fruit.getRfidNumber());
        product_name.setText(fruit.getProductName());
        return view;
    }

}
