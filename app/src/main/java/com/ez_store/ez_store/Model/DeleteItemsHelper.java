package com.ez_store.ez_store.Model;

public class DeleteItemsHelper {
    String itemName, itemQuantity, itemImage,id;

    public DeleteItemsHelper(String id,String itemName, String itemQuantity, String itemImage) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemImage = itemImage;
        this.id=id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }
}
