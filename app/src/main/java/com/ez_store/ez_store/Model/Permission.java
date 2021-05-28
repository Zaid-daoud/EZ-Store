package com.ez_store.ez_store.Model;

public class Permission {
    String id, perm_name;

    public Permission(String id, String perm_name) {
        this.id = id;
        this.perm_name = perm_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPerm_name() {
        return perm_name;
    }

    public void setPerm_name(String perm_name) {
        this.perm_name = perm_name;
    }
}
