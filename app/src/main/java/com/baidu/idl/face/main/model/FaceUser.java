package com.baidu.idl.face.main.model;

public class FaceUser {
    private String isFace;
    private String name;
    private String phone;
    private int id;
    public String getIsFace() {
        return isFace;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public int getId() {
        return id;
    }
    public FaceUser( String name, String phone,String  isFace,int id) {
        this.isFace = isFace;
        this.name = name;
        this.phone = phone;
        this.id = id;
    }
}


