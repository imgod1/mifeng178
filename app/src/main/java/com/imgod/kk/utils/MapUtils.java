package com.imgod.kk.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * map和bean的相互转换
 */
public class MapUtils {
    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null)
            return null;

        Object obj = beanClass.newInstance();

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }

            field.setAccessible(true);
            field.set(obj, map.get(field.getName()));
        }

        return obj;
    }

    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }

        return map;
    }

    //treemap 内部 会把key排序
    public static TreeMap<String, Object> objectToTreeMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }

        TreeMap<String, Object> map = new TreeMap<>();
        addObjectAttribute2Map(obj,obj.getClass(), map);//加自己的属性
        addObjectAttribute2Map(obj,obj.getClass().getSuperclass(), map);//加父类的属性
        return map;
    }

    private static void addObjectAttribute2Map(Object object,Class tempClass, Map map) throws Exception {
        Field[] declaredFields = tempClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }
    }
}
