package org.yml.plugin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;

import java.nio.charset.StandardCharsets;

/**
 * @author yaml
 * @since 2021/2/1
 */
public class HttpUtils {

    /**
     * 请求超时时间设置(10秒)
     */
    private static final int TIMEOUT = 10 * 1000;

    /**
     * http客户端
     */
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    public static JSONObject postJson(String url, Object body) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(buildDefaultConfig());
        httpPost.setEntity(new StringEntity(JsonUtil.toJson(body), ContentType.APPLICATION_JSON));
        final CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
        Assert.assertEquals("上报接口异常", 200, response.getStatusLine().getStatusCode());
        return JSON.parseObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
    }

    private static RequestConfig buildDefaultConfig() {
        return RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build();
    }

}
