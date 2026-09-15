package com.example.a0731;

public class WindowFrame {
    private String frameNumber;
    private String floor;
    private String layout;
    private String length;
    private String width;
    private String quantity;
    private String specification;

    public WindowFrame(String frameNumber, String floor, String layout, String length, String width, String quantity, String specification) {
        this.frameNumber = frameNumber;
        this.floor = floor;
        this.layout = layout;
        this.length = length;
        this.width = width;
        this.quantity = quantity;
        this.specification = specification;
    }

    // Getter 方法
    public String getFrameNumber() {
        return frameNumber;
    }

    public String getFloor() {
        return floor;
    }

    public String getLayout() {
        return layout;
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

    public String getSpecification() {
        return specification;
    }

    // Setter 方法（如果需要）
    public void setFrameNumber(String frameNumber) {
        this.frameNumber = frameNumber;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }
}
