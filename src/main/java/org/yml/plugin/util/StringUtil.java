package org.yml.plugin.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author yaml
 * @since 2022/1/11
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {

    public static String removeStart(String str, String start) {

        if (str == null || str.isEmpty() || !str.startsWith(start)) {
            return str;
        }
        str = str.substring(start.length());
        return removeStart(str, start);
    }
}
