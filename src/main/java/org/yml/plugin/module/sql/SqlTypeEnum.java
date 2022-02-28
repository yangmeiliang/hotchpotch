package org.yml.plugin.module.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yml.plugin.util.NameUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yaml
 * @since 2021/2/24
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("ALL")
public enum SqlTypeEnum {

    UNKNOWN("Object", "java.lang.Object"),
    TYPE_01("varchar(\\(\\d+\\))?", "java.lang.String"),
    TYPE_02("char(\\(\\d+\\))?", "java.lang.String"),
    TYPE_03("text", "java.lang.String"),
    TYPE_04("decimal(\\(\\d+\\))?", "java.lang.Double"),
    TYPE_05("decimal(\\(\\d+,\\d+\\))?", "java.lang.Double"),
    TYPE_06("integer", "java.lang.Integer"),
    TYPE_07("int(\\(\\d+\\))?", "java.lang.Integer"),
    TYPE_08("int4", "java.lang.Integer"),
    TYPE_09("int8", "java.lang.Long"),
    TYPE_10("bigint(\\(\\d+\\))?", "java.lang.Long"),
    TYPE_11("datetime", "java.util.Date"),
    TYPE_12("timestamp", "java.util.Date"),
    TYPE_13("boolean", "java.lang.Boolean"),
    TYPE_14("tinyint(\\(\\d+\\))?", "java.lang.Integer"),
    TYPE_15("smallint(\\(\\d+\\))?", "java.lang.Integer"),
    ;

    private String sqlTypeRegex;
    private String javaTypeFullName;

    public static final Map<SqlTypeEnum, Pattern> patternMap = new HashMap<>(13);

    static {
        for (SqlTypeEnum sqlTypeEnum : SqlTypeEnum.values()) {
            patternMap.put(sqlTypeEnum, Pattern.compile(sqlTypeEnum.sqlTypeRegex, Pattern.CASE_INSENSITIVE));
        }
    }

    public static String allRegex() {
        return Arrays.stream(SqlTypeEnum.values())
                .map(SqlTypeEnum::getSqlTypeRegex)
                .collect(Collectors.joining("|"));
    }

    public static SqlTypeEnum of(final String sqlType) {
        return patternMap.entrySet().stream()
                .filter(entry -> entry.getValue().matcher(sqlType).matches())
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(UNKNOWN);
    }

    /**
     * 获取sql类型对应的java类型简写
     *
     * @param sqlTypeRegex SQL类型正则表达式
     */
    public static String getJavaTypeShortName(final String sqlType) {
        return patternMap.entrySet().stream()
                .filter(entry -> entry.getValue().matcher(sqlType).matches())
                .findFirst()
                .map(Map.Entry::getKey)
                .map(SqlTypeEnum::getJavaTypeShortName)
                .orElse("Object");
    }

    public String getJavaTypeShortName() {
        return NameUtils.getClsNameByFullName(this.javaTypeFullName);
    }
}
