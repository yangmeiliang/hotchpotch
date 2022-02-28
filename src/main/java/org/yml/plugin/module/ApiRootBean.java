package org.yml.plugin.module;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author yaml
 * @since 2021/1/6
 */
@Data
@Accessors(chain = true)
public class ApiRootBean {

    private String basePath;

    private MethodInfo.FieldItem globalReturnClass;

    private List<MethodInfo> methodInfos;

}
