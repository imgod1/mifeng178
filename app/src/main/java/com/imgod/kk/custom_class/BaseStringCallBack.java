package com.imgod.kk.custom_class;

import android.text.TextUtils;

import com.imgod.kk.app.Constants;
import com.imgod.kk.app.MiFengApplication;
import com.imgod.kk.response_model.BaseResponse;
import com.imgod.kk.utils.GsonUtil;
import com.imgod.kk.utils.ToastUtils;
import com.zhy.http.okhttp.callback.StringCallback;

public abstract class BaseStringCallBack extends StringCallback {
    @Override
    public void onResponse(String response, int id) {
        if (!TextUtils.isEmpty(response)) {
            BaseResponse baseResponse = GsonUtil.GsonToBean(response, BaseResponse.class);
            if (baseResponse.getRet() == Constants.REQUEST_STATUS.TOKEN_OUT_TIME && baseResponse.getMsg().equals("用户登录失败")) {
                ToastUtils.showToastShort(MiFengApplication.context, "请重新登陆");

                return;
            }
        }
    }
}
