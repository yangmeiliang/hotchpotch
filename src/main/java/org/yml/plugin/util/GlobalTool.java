package org.yml.plugin.util;

import com.intellij.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全局工具类
 *
 * @author yaml
 * @since 2021/2/5
 */
public interface GlobalTool {


    /**
     * 创建集合
     *
     * @param items 初始元素
     * @return 集合对象
     */
    static Set<?> newHashSet(Object... items) {
        return items == null ? new HashSet<>() : new HashSet<>(Arrays.asList(items));
    }

    /**
     * 创建列表
     *
     * @param items 初始元素
     * @return 列表对象
     */
    static List<?> newArrayList(Object... items) {
        return items == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(items));
    }

    /**
     * 创建有序Map
     *
     * @return map对象
     */
    static Map<?, ?> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * 创建无序Map
     *
     * @return map对象
     */
    static Map<?, ?> newHashMap() {
        return new HashMap<>(16);
    }

    /**
     * 获取字段，私有属性一样强制访问
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     */
    static Object getField(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Class<?> cls = obj.getClass();
        return ReflectionUtil.getField(cls, obj, Object.class, fieldName);
    }

    /**
     * 无返回执行，用于消除返回值
     *
     * @param obj 接收执行返回值
     */
    static void call(Object... obj) {

    }

    /**
     * 获取某个类的所有字段
     *
     * @param cls 类
     * @return 所有字段
     */
    static List<Field> getAllFieldByClass(Class<?> cls) {
        List<Field> result = new ArrayList<>();
        do {
            result.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        } while (!cls.equals(Object.class));
        return result;
    }

    static String removeImpl(String name) {
        if (name.endsWith("Impl")) {
            return name.replace("Impl", "");
        }
        return name;
    }

    static String append(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg);
        }
        return stringBuilder.toString();
    }
}
