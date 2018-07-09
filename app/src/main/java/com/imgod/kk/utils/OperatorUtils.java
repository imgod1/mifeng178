package com.imgod.kk.utils;

import com.imgod.kk.app.Constants;

/**
 * OperatorUtils.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/7/9 10:53
 * @update imgod1 2018/7/9 10:53
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class OperatorUtils {
    public static String getOperatorNameByType(int type) {
        switch (type) {
            case Constants.OPERATOR_TYPE.MOBILE:
                return "移动";
            case Constants.OPERATOR_TYPE.UNICOM:
                return "联通";
            case Constants.OPERATOR_TYPE.TELECOM:
                return "电信";
            default:
                return "不限";
        }
    }
}
