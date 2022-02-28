package org.yml.plugin.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yaml
 * @since 2021/3/1
 */
public interface PatternUtils {

    static String groupFirst(@NotNull Pattern pattern, String text) {
        return Optional.of(pattern.matcher(text))
                .filter(Matcher::find)
                .map(Matcher::group)
                .orElse("");
    }

    static List<String> groupAll(@NotNull Pattern pattern, String text) {
        List<String> data = new ArrayList<>();
        if (StringUtils.isBlank(text)) {
            return data;
        }
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return data;
        }
        for (int i = 0; i < matcher.groupCount(); i++) {
            data.add(matcher.group(i));
        }
        return data;
    }
}
