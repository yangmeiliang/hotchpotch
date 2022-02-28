package org.yml.plugin.enums;

/**
 * @author yaml
 * @since 2020/12/29
 */
public enum RequestMethodEnum {

    GET, POST, PUT, PATCH, DELETE;

    public static RequestMethodEnum resolve(String text) {
        if (text == null) {
            return null;
        }
        if (text.toUpperCase().contains(GET.name())) {
            return GET;
        }
        if (text.toUpperCase().contains(POST.name())) {
            return POST;
        }
        if (text.toUpperCase().contains(PUT.name())) {
            return PUT;
        }
        if (text.toUpperCase().contains(PATCH.name())) {
            return PATCH;
        }
        if (text.toUpperCase().contains(DELETE.name())) {
            return DELETE;
        }
        return null;
    }
}
