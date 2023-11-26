package com.baidu.idl.face.main.model;

public class Goods {
    private String rfidNumber;
    private String productNumber;
    private String productName;
    private int id;
    public  String warehouseIdName;
    public  String productTypeIdName;
    public String getProductNumber() {
        return productNumber;
    }
    public String getProductName() {
        return productName;
    }
    public String getRfidNumber() {
        return rfidNumber;
    }
    public String getwarehouseIdName() {
        return warehouseIdName;
    }
    public String getProductTypeIdName() {
        return productTypeIdName;
    }
    public int getId() {
        return id;
    }
    public Goods( String productNumber, String productName,String  rfidNumber,String warehouseIdName,String productTypeIdName,int id) {
        this.productNumber = productNumber;
        this.productName = productName;
        this.rfidNumber = rfidNumber;
        this.warehouseIdName = warehouseIdName;
        this.productTypeIdName = productTypeIdName;
        this.id = id;
    }
}


