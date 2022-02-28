package org.yml.plugin.properties;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.PropertiesUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.yml.plugin.util.NotificationUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author yaml
 * @since 2020/12/28
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemProperties {

    /**
     * dev环境域名
     */
    private String devHost;
    /**
     * prd环境域名
     */
    private String prdHost;
    /**
     * 项目token
     */
    private String projectToken;
    /**
     * 0 追加导入，2：智能覆盖
     */
    private String importType = "2";
    /**
     * apiMocker上传接口
     */
    private String uploadUrl;
    /**
     * 统一返回封装类
     */
    private String returnClass;


    public static SystemProperties getInstance(@NotNull Project project) {
        String basePath = project.getBasePath();
        String propertiesFilePath = basePath + "/src/main/resources/api-mocker.properties";
        Map<String, String> properties = null;
        try {
            properties = PropertiesUtil.loadProperties(Files.newBufferedReader(Paths.get(propertiesFilePath)));
        } catch (IOException ignore) {
        }
        if (properties == null) {
            NotificationUtils.error("file api-mocker.properties not found");
        }
        assert properties != null;
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.setDevHost(properties.get("devHost"));
        systemProperties.setPrdHost(properties.get("prdHost"));
        systemProperties.setImportType(properties.get("importType"));
        systemProperties.setProjectToken(properties.get("projectToken"));
        systemProperties.setUploadUrl(properties.get("uploadUrl"));
        systemProperties.setReturnClass(properties.get("returnClass"));
        return systemProperties;
    }
}
