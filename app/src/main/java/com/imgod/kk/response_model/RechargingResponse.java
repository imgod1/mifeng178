package com.imgod.kk.response_model;

import java.util.List;

/**
 * RechargingResponse.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/7/9 14:12
 * @update imgod1 2018/7/9 14:12
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class RechargingResponse extends BaseResponse {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 22234234
         * prov : 江苏
         * mobile : 13800138000
         * amount : 100
         * create_time : 5646545646
         */

        private String id;
        private String prov;
        private String mobile;
        private int amount;
        private long create_time;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProv() {
            return prov;
        }

        public void setProv(String prov) {
            this.prov = prov;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public long getCreate_time() {
            return create_time;
        }

        public void setCreate_time(long create_time) {
            this.create_time = create_time;
        }
    }
}
