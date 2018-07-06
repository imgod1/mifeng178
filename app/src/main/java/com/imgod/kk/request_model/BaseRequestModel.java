package com.imgod.kk.request_model;

import com.imgod.kk.app.Constants;

/**
 * BaseRequestModel.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/7/6 16:46
 * @update imgod1 2018/7/6 16:46
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class BaseRequestModel {

    /**
     * action : login
     * app_key : xxxxx
     * time : 21212323
     * token : xxxx
     * sign : xxxxx
     */

    private String action;
    private String app_key = Constants.MIFENG_KEY;
    private long time;
    private String token;
    private String sign;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
