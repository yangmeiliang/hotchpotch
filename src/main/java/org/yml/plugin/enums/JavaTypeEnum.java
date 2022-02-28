package org.yml.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yaml
 * @since 2020/12/31
 */
@Getter
@AllArgsConstructor
public enum JavaTypeEnum {

    VOID("void", null, null),
    PRIM_int("int", 0, MockerTypeEnum.Number),
    PRIM_boolean("boolean", false, MockerTypeEnum.Boolean),
    PRIM_byte("byte", (byte) 0, MockerTypeEnum.String),
    PRIM_short("short", 0, MockerTypeEnum.Number),
    PRIM_long("long", 0L, MockerTypeEnum.Number),
    PRIM_float("float", 0.1F, MockerTypeEnum.Number),
    PRIM_double("double", 0.1D, MockerTypeEnum.Number),
    PRIM_char("char", 'a', MockerTypeEnum.String),

    WRAP_Boolean("Boolean", false, MockerTypeEnum.Boolean),
    WRAP_Byte("Byte", (byte) 0, MockerTypeEnum.String),
    WRAP_Short("Short", 0, MockerTypeEnum.Number),
    WRAP_Integer("Integer", 0, MockerTypeEnum.Number),
    WRAP_Long("Long", 0L, MockerTypeEnum.Number),
    WRAP_Float("Float", 0.1F, MockerTypeEnum.Number),
    WRAP_Double("Double", 0.1D, MockerTypeEnum.Number),

    REFER_String("String", "", MockerTypeEnum.String),
    REFER_BigDecimal("BigDecimal", new BigDecimal("0.1"), MockerTypeEnum.Number),
    REFER_Date("Date", new Date(), MockerTypeEnum.Number),
    REFER_LocalDate("LocalDate", LocalDate.now(), MockerTypeEnum.Number),
    REFER_LocalTime("LocalTime", LocalTime.now(), MockerTypeEnum.Number),
    REFER_LocalDateTime("LocalDateTime", LocalDateTime.now(), MockerTypeEnum.Number),
    REFER_Timestamp("Timestamp", Timestamp.valueOf(LocalDateTime.now()), MockerTypeEnum.Number),
    ;

    private final String shortName;
    private final Object defaultValue;
    private final MockerTypeEnum mockerType;

    public static boolean exists(String shortName) {
        return false;
    }


    public boolean eq(String shortName) {
        return Objects.equals(shortName, this.shortName);
    }

    public static Optional<JavaTypeEnum> of(String shortName) {
        return Arrays.stream(JavaTypeEnum.values()).filter(o -> o.eq(shortName)).findFirst();
    }
}
