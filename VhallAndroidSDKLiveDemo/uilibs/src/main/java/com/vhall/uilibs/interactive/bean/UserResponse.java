package com.vhall.uilibs.interactive.bean;

import java.io.Serializable;

/**
 * @author hkl
 * Date: 2019-12-31 10:15
 */
public class UserResponse implements Serializable {


    /**
     * user_id : 18
     * nick_name : eeeee
     * phone : 13303939393
     * token : 47ae028bb0810df5
     * icon :1 头像
     */
    private String token;
    private String user_id;
    private String name;
    private String nick_name;
    private String avatar;
    private String phone;
    private int is_new_regist;
    private int is_jump_hd;

    public UserResponse() {
    }

    public int getIs_new_regist() {
        return is_new_regist;
    }

    public void setIs_new_regist(int is_new_regist) {
        this.is_new_regist = is_new_regist;
    }

    public int getIs_jump_hd() {
        return is_jump_hd;
    }

    public void setIs_jump_hd(int is_jump_hd) {
        this.is_jump_hd = is_jump_hd;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getnick_name() {
        return nick_name;
    }

    public void setnick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }


}
