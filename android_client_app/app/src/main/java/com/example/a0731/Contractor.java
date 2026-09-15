package com.example.a0731;

public class Contractor {
    private String name;
    private String phone;

    // 構造函數，用來初始化承包商的名字和電話
    public Contractor(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // 獲取承包商名字的方法
    public String getName() {
        return name;
    }

    // 獲取承包商電話的方法
    public String getPhone() {
        return phone;
    }
}
