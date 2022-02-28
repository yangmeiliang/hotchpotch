package org.yml.plugin.properties;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.PropertiesUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.yml.plugin.util.OptionalUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * @author yaml
 * @since 2020/12/28
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ApiMockerProperties {

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
     * 用户token
     */
    private String userToken;
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

    public boolean validate() {
        return !StringUtils.isAnyBlank(projectToken, uploadUrl);
    }


    public static ApiMockerProperties getInstance(Project project, String path) {
        ApiMockerProperties apiMockerProperties = resolveFromApiMockerProperties(project, path);
        if (apiMockerProperties == null) {
            apiMockerProperties = resolveFromProjectFile(project);
        }
        if (apiMockerProperties == null) {
            apiMockerProperties = new ApiMockerProperties();
        }
        return apiMockerProperties;
    }

    private static ApiMockerProperties resolveFromApiMockerProperties(Project project, String path) {
        try {
            String propertiesFilePath = path;
            if(!path.endsWith("api-mocker.properties")){
                int index = path.indexOf("/src/main/java");
                propertiesFilePath = (index == -1 ? path : path.substring(0, index)) + "/src/main/resources/api-mocker.properties";
            }
            Map<String, String> properties = PropertiesUtil.loadProperties(Files.newBufferedReader(Paths.get(propertiesFilePath)));
            if (properties.isEmpty()) {
                return null;
            }
            ApiMockerProperties apiMockerProperties = new ApiMockerProperties();
            apiMockerProperties.setDevHost(properties.get("devHost"));
            apiMockerProperties.setPrdHost(properties.get("prdHost"));
            apiMockerProperties.setImportType(properties.get("importType"));
            apiMockerProperties.setProjectToken(properties.get("projectToken"));
            apiMockerProperties.setUserToken(properties.get("userToken"));
            apiMockerProperties.setUploadUrl(properties.get("uploadUrl"));
            apiMockerProperties.setReturnClass(properties.get("returnClass"));
            if (StringUtils.isBlank(apiMockerProperties.getUserToken())) {
                apiMockerProperties.setUserToken(resolveApiMockerValue(project, "userToken"));
            }
            return apiMockerProperties;
        } catch (IOException ignore) {
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static ApiMockerProperties resolveFromProjectFile(Project project) {
        try {
            VirtualFile projectFile = project.getProjectFile();
            String data = new String(projectFile.contentsToByteArray(), StandardCharsets.UTF_8);
            String apiMocker = data.split("<api-mocker>")[1];
            if (StringUtils.isBlank(apiMocker)) {
                return null;
            }
            ApiMockerProperties apiMockerProperties = new ApiMockerProperties();
            apiMockerProperties.setDevHost(resolveApiMockerValue(apiMocker, "devHost"));
            apiMockerProperties.setPrdHost(resolveApiMockerValue(apiMocker, "prdHost"));
            apiMockerProperties.setProjectToken(resolveApiMockerValue(apiMocker, "projectToken"));
            apiMockerProperties.setUserToken(resolveApiMockerValue(apiMocker, "userToken"));
            apiMockerProperties.setImportType(resolveApiMockerValue(apiMocker, "importType"));
            apiMockerProperties.setUploadUrl(resolveApiMockerValue(apiMocker, "uploadUrl"));
            apiMockerProperties.setReturnClass(resolveApiMockerValue(apiMocker, "returnClass"));
            return apiMockerProperties;
        } catch (Exception ignore) {
            return null;
        }

    }

    private static String resolveApiMockerValue(String apiMocker, String elementName) {
        return OptionalUtil.ofBlank(apiMocker)
                .map(s -> s.split("<" + elementName + ">")[1])
                .map(s -> s.split("</" + elementName + ">")[0])
                .orElse("");
    }

    @SuppressWarnings("ConstantConditions")
    private static String resolveApiMockerValue(Project project, String elementName) {
        try {
            VirtualFile projectFile = project.getProjectFile();
            String data = new String(projectFile.contentsToByteArray(), StandardCharsets.UTF_8);
            return resolveApiMockerValue(data.split("<api-mocker>")[1], elementName);
        } catch (Exception ignore) {

        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ApiMockerProperties)) {
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
