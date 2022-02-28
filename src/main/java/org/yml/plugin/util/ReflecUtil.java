package org.yml.plugin.util;

import com.intellij.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author yaml
 * @since 2021/7/14
 */
@SuppressWarnings("ALL")
public class ReflecUtil {

    public static <T> T invoke(Object obj, String methodName, Object... parameters) {
        try {
            Class<?>[] objects = Arrays.stream(parameters).map(Object::getClass).toArray(Class[]::new);
            Method method = ReflectionUtil.getMethod(obj.getClass(), methodName, objects);
            return (T) method.invoke(obj, parameters);
        } catch (Exception ignore) {
        }
        return (T) null;
    }
}
