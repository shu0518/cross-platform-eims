package com.example.a0731;

public class Quotation {
    private String ProjectName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String length;
    private String width;

    public Quotation(String ProjectName, String length, String width, int quantity, double unitPrice, double totalPrice) {
        this.ProjectName = ProjectName;
        this.length = length;
        this.width = width;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public String getProjectName() {
        return ProjectName;
    }

    public String getLength() {
        return length;
    }

    public String getWidth() {
        return width;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
