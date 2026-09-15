// ProcessingData 類別 (假設已有的數據模型)
package com.example.a0731;
public class ProcessingData {
    private String spec;
    private String specification;
    private String location;
    private String length;
    private String width;
    private String quantity;
    private String id;
    private boolean isDoor;
    private boolean isSelected; // 新增的選擇狀態

    // 原有的構造函數：6 個參數
    public ProcessingData(String id, String specification, String spec, String location, String length, String width, String quantity, boolean isDoor) {
        this.id=id;
        this.specification= specification;
        this.spec = spec;
        this.location = location;
        this.length = length;
        this.width = width;
        this.quantity = quantity;
        this.isDoor = isDoor;
    }

    // 新增的重載構造函數：3 個參數，使用預設值
//    public ProcessingData(String frameNumber, String floor, String layout) {
//        this.frameNumber = frameNumber;
//        this.floor = floor;
//        this.layout = layout;
//        this.screenWindowHori = 0.0;  // 預設值
//        this.glassWidth = 0.0;        // 預設值
//        this.glassLength = 0.0;       // 預設值
//        this.isSelected = false;      // 預設值為未選中
//    }
//
    // 新增的重載構造函數：7 個參數，包含選中狀態
//    public ProcessingData(String frameNumber, String floor, String layout,
//                          double screenWindowHori, double glassWidth, double glassLength, boolean isSelected) {
//        this.frameNumber = frameNumber;
//        this.floor = floor;
//        this.layout = layout;
//        this.screenWindowHori = screenWindowHori;
//        this.glassWidth = glassWidth;
//        this.glassLength = glassLength;
//        this.isSelected = isSelected; // 指定是否選中
//    }

    // Getter 方法
    public String getid(){return id;}
    public String getspec() {
        return spec;
    }
    public String getSpecification() { return specification; }
    // Getter 和 Setter 方法
    public String getLocation() {
        return location;
    }

    public String getLength() {
        return length;
    }

    public String getWidth() {
        return width;
    }

    public String getQuantity() {
        return quantity;
    }

    public boolean isDoor() {
        return isDoor;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
