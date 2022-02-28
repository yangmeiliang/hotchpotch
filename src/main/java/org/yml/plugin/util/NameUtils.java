package org.yml.plugin.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命名工具类
 *
 * @author yaml
 * @since 2021/2/5
 */
public interface NameUtils {

    /**
     * 驼峰命名正则匹配规则
     */
    Pattern TO_HUMP_PATTERN = Pattern.compile("[-_]([a-z0-9A-Z])");
    Pattern TO_LINE_PATTERN = Pattern.compile("[A-Z]+");


    /**
     * 驼峰转下划线，全小写
     * helloWorld => hello_world
     * HelloWorld => hello_world
     */
    static String toUnderline(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        Matcher matcher = TO_LINE_PATTERN.matcher(str);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (matcher.start() > 0) {
                matcher.appendReplacement(buffer, "_" + matcher.group(0).toLowerCase());
            } else {
                matcher.appendReplacement(buffer, matcher.group(0).toLowerCase());
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 下划线中横线命名转驼峰命名
     * hello-world => helloWorld
     * hello_world => helloWorld
     * Hello-World => helloWorld
     */
    static String toHump(final String name) {
        if (StringUtils.isBlank(name) || !StringUtils.containsAny(name, "-", "_")) {
            return name;
        }
        Matcher matcher = TO_HUMP_PATTERN.matcher(name);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 首字母大写方法
     *
     * @param name 名称
     * @return 结果
     */
    static String firstUpperCase(String name) {
        return StringUtils.capitalize(name);
    }

    /**
     * 首字母小写方法
     *
     * @param name 名称
     * @return 结果
     */
    static String firstLowerCase(String name) {
        return StringUtils.uncapitalize(name);
    }

    /**
     * 任意对象合并工具类
     *
     * @param objects 任意对象
     * @return 合并后的字符串结果
     */
    static String append(Object... objects) {

        if (objects == null || objects.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Object s : objects) {
            if (s != null) {
                builder.append(s);
            }
        }
        return builder.toString();
    }

    /**
     * 通过java全名获取类名
     *
     * @param fullName 全名
     * @return 类名
     */
    static String getClsNameByFullName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    /**
     * 下划线中横线命名转驼峰命名（类名）
     *
     * @param name 名称
     * @return 结果
     */
    static String getClassName(String name) {
        return firstUpperCase(toHump(name));
    }

    static String parsePackage2Path(String packagePath) {
        if (!packagePath.contains(".")) {
            return packagePath;
        }
        return "/" + packagePath.replace(".", "/");
    }
}
