package com.imgod.kk.utils;

import com.imgod.kk.request_model.BaseRequestModel;

public class ModelUtils {
    public static void initModelSign(BaseRequestModel baseRequestModel) {
        String sign = SignUtils.getSortedKeyStringFromObject(baseRequestModel);
        String md5Sign = MD5Utils.EncoderByMD5(sign);
        baseRequestModel.setSign(md5Sign);
    }
}
