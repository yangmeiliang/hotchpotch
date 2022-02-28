package org.yml.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yaml
 * @since 2020/12/30
 */
@Getter
@AllArgsConstructor
public enum SwaggerMockTypeEnum {

    Number("number"),
    String("string"),
    Boolean("boolean"),
    Object("object"),
    Array("array");
    private final String typeName;
}
