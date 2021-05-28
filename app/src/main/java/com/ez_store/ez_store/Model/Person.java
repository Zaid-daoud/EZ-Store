package com.ez_store.ez_store.Model;


public class Person {
    private String ID , UserName , Password , Phone ,Email ,role ,imgUri ,Stakeholder;
    private boolean Employed;

    public Person() {
        Employed = false;
    }

    public Person(String id , String userName, String password, String phone , String email,String role,boolean employed) {
        UserName = userName;
        Password = password;
        Phone = phone;
        Email = email;
        ID = id;
        this.role = role;
        Employed = employed;
    }

    public  String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        this.Phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public boolean isEmployed() {
        return Employed;
    }

    public void setEmployed(boolean employed) {
        Employed = employed;
    }

    public String getStakeholder() {
        return Stakeholder;
    }

    public void setStakeholder(String stakeholder) {
        Stakeholder = stakeholder;
    }
}
