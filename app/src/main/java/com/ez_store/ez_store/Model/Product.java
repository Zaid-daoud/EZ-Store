package com.ez_store.ez_store.Model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Product {
    private String barCode , name , description ,imgUri ,expiredDate;
    private int quantity;
    private double price;
    public Product() {

    }

    public Product(String barCode ,String name,String description, String imgUri ,String price,String quantity , Date expiredDate) {
        this.barCode = barCode;
        this.name = name;
        this.imgUri = imgUri;
        this.description = description;
        this.price = Double.parseDouble(price); // convert price to double
        this.quantity = Integer.parseInt(quantity);
        this.expiredDate = new SimpleDateFormat("dd/MM/yyyy").format(expiredDate);
    }

    public Product(String id, String name, String quantity, String date) {
        this.barCode =id;
        this.name = name;
        this.quantity = Integer.parseInt(quantity);
        this.expiredDate = date;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
