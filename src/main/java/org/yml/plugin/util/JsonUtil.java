package org.yml.plugin.util;

import com.alibaba.fastjson.JSON;

/**
 * @author yaml
 * @since 2021/1/6
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String toPrettyJson(Object obj) {
        return JSON.toJSONString(obj, true);
    }
}
