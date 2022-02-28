package org.yml.plugin.module;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yaml
 * @since 2021/3/16
 */
@Getter
@Setter
public class VmTemplate {

    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板路径
     */
    private String path;

    public static VmTemplate create(String templatePath) {
        String name = templatePath.substring(templatePath.lastIndexOf("/") + 1).replace(".vm", "");
        VmTemplate template = new VmTemplate();
        template.setName(name);
        template.setPath(templatePath);
        return template;
    }
}
