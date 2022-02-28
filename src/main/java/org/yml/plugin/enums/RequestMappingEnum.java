package org.yml.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author yaml
 * @since 2020/12/29
 */
@Getter
@AllArgsConstructor
public enum RequestMappingEnum {

    RequestMapping("org.springframework.web.bind.annotation.RequestMapping", null),
    GetMapping("org.springframework.web.bind.annotation.GetMapping", RequestMethodEnum.GET),
    PostMapping("org.springframework.web.bind.annotation.PostMapping", RequestMethodEnum.POST),
    PutMapping("org.springframework.web.bind.annotation.PutMapping", RequestMethodEnum.PUT),
    DeleteMapping("org.springframework.web.bind.annotation.DeleteMapping", RequestMethodEnum.DELETE),
    PatchMapping("org.springframework.web.bind.annotation.PatchMapping", RequestMethodEnum.PATCH),
    ;


    private final String reference;
    private final RequestMethodEnum requestMethodEnum;

    public boolean eq(String reference) {
        return Objects.equals(reference, this.reference);
    }

    public static RequestMappingEnum of(String reference) {
        return Arrays.stream(RequestMappingEnum.values()).filter(o -> o.eq(reference)).findFirst().orElse(null);
    }
}
