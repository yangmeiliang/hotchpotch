package org.yml.plugin.util;


import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Optional;

/**
 * @author yaml
 * @since 2020/12/28
 */
public class OptionalUtil {

    public static <T extends Collection<?>> Optional<T> ofEmpty(T value) {
        return (value == null || value.isEmpty()) ? Optional.empty() : Optional.of(value);
    }

    public static Optional<String> ofBlank(String value) {
        return StringUtils.isBlank(value) ? Optional.empty() : Optional.of(value);
    }

    public static Optional<Integer> ofPositive(Integer value) {
        return value == null || value < 1 ? Optional.empty() : Optional.of(value);
    }
}
