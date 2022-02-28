package org.yml.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author yaml
 * @since 2020/12/30
 */
@Getter
@AllArgsConstructor
public enum MockerTypeEnum {

    /**
     *
     */
    Number("number"),
    String("string"),
    Boolean("boolean"),
    Object("object"),
    Array("array");

    private final String typeName;


    public boolean anyMatch(MockerTypeEnum... array) {
        return Arrays.stream(array).anyMatch(a -> a == this);
    }
}
