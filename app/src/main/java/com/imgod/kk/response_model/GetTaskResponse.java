package com.imgod.kk.response_model;

public class GetTaskResponse extends BaseResponse {

    /**
     * data : {"id":22234234,"prov":"江苏","mobile":"13800138000","amount":100,"timeout":15018415241}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 22234234
         * prov : 江苏
         * mobile : 13800138000
         * amount : 100
         * timeout : 15018415241
         */

        private String id;
        private String prov;
        private String mobile;
        private String amount;
        private String timeout;

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

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }
    }
}
