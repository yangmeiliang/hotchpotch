package org.yml.plugin.module;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.yml.plugin.properties.ApiMockerProperties;

import java.util.Collections;
import java.util.List;

/**
 * @author yaml
 * @since 2021/1/27
 */
@Getter
@Setter
public class ApiMockerRequest {

    private Integer importType = 2;
    private String token;
    private String userToken;
    private String devUrl;
    private String prodUrl;
    private Object json = new JSONObject();
    private Object swaggerJson = new JSONObject();
    private List<Boolean> apis = Collections.singletonList(false);

    public static ApiMockerRequest instance(ApiMockerProperties apiMockerProperties, Object swaggerJson) {
        ApiMockerRequest apiMockerRequest = new ApiMockerRequest();
        apiMockerRequest.setImportType(Integer.valueOf(apiMockerProperties.getImportType()));
        apiMockerRequest.setToken(apiMockerProperties.getProjectToken());
        apiMockerRequest.setUserToken(apiMockerProperties.getUserToken());
        apiMockerRequest.setDevUrl(apiMockerProperties.getDevHost());
        apiMockerRequest.setProdUrl(apiMockerProperties.getPrdHost());
        apiMockerRequest.setJson(swaggerJson);
        return apiMockerRequest;
    }

}
