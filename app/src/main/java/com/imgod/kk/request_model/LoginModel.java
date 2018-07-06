package com.imgod.kk.request_model;

/**
 * LoginModel.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/7/6 16:54
 * @update imgod1 2018/7/6 16:54
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class LoginModel extends BaseRequestModel {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
