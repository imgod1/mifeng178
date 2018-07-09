package com.imgod.kk.request_model;

/**
 * ReportModel.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/7/9 9:31
 * @update imgod1 2018/7/9 9:31
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class ReportModel extends BaseRequestModel {

    /**
     * id : 1231231
     * mobile : 13800138000
     * result : 1
     * voucher : data:image/jpg;base64,/9j/4QMZR...
     */

    private String id;
    private String mobile;
    private int result;
    private String voucher;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }
}
