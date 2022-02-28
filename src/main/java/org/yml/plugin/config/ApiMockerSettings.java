package org.yml.plugin.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.Data;
import org.yml.plugin.properties.ApiMockerProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author yaml
 * @since 2021/3/16
 */
@Data
@State(name = "ApiMockerSettings", storages = @Storage("api-mocker-settings.xml"))
public class ApiMockerSettings implements PersistentStateComponent<ApiMockerSettings>, Cloneable {

    private LinkedHashMap<String, ApiMockerProperties> projectConfigMap;

    public static ApiMockerSettings getInstance() {
        return ServiceManager.getService(ApiMockerSettings.class);
    }

    public ApiMockerSettings() {
        projectConfigMap = new LinkedHashMap<>();
    }

    @Override
    public @Nullable ApiMockerSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiMockerSettings settings) {
        // 覆盖初始配置
        this.setProjectConfigMap(settings.getProjectConfigMap());
    }

    @Override
    public void noStateLoaded() {

    }

    @Override
    public void initializeComponent() {

    }

    @Override
    public ApiMockerSettings clone() {
        return JSON.parseObject(JSON.toJSONString(this), ApiMockerSettings.class);
    }

    public ApiMockerProperties getConfig(String projectName) {
        return projectConfigMap.get(projectName);
    }

    public void overwrite(ApiMockerSettings apiMockerSettings) {
        this.projectConfigMap = apiMockerSettings.clone().getProjectConfigMap();
    }

    public ApiMockerProperties getConfigByPath(Project project, String path) {
        String configName = resolveConfigName(path);
        ApiMockerProperties properties = projectConfigMap.get(configName);
        if (properties != null) {
            return properties;
        }
        ApiMockerProperties instance = ApiMockerProperties.getInstance(project, path);
        if (instance.validate()) {
            projectConfigMap.put(configName, instance);
        }
        return instance;
    }

    private String resolveConfigName(String path) {
        int index = path.indexOf("/src/main/java");
        String substring = path.substring(0, index);
        String configName = substring.substring(substring.lastIndexOf("/"));
        return configName.replace("/", "");
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ApiMockerSettings)) {
            return false;
        } else {
            return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue).equals(JSON.toJSONString(o, SerializerFeature.WriteMapNullValue));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(JSON.toJSONString(this));
    }
}
