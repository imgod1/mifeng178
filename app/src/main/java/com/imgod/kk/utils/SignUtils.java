package com.imgod.kk.utils;

import com.imgod.kk.app.Constants;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class SignUtils {
    public static String getSortedKeyStringFromObject(Object object) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            TreeMap treeMap = MapUtils.objectToTreeMap(object);
            Set<String> keySet = treeMap.keySet();
            Iterator<String> iter = keySet.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                if (!"sign".equals(key)) {//把sign字段排除
                    stringBuilder.append(key);
                    if (null != treeMap.get(key)) {
                        stringBuilder.append(treeMap.get(key));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString() + Constants.MIFENG_SECRET;
    }
}
