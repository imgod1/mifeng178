package com.imgod.kk.response_model;

public class BaseResponse {

    /**
     * ret : 0
     * msg : LOGIN SUCCESS
     */

    private int ret;
    private String msg;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
