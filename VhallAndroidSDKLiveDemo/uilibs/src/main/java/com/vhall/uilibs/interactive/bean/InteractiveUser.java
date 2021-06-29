package com.vhall.uilibs.interactive.bean;

public class InteractiveUser {
    public String name = "未知";
    public String role="2";
    public String avatar;

    public InteractiveUser() {
    }

    public InteractiveUser(String name, String role, String avatar) {
        this.name = name;
        this.role = role;
        this.avatar = avatar;
    }
}
