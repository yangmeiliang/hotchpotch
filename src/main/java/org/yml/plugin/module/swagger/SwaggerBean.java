package org.yml.plugin.module.swagger;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.yml.plugin.module.MethodInfo;
import org.yml.plugin.util.JsonUtil;
import org.yml.plugin.util.OptionalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author yaml
 * @since 2021/1/5
 */
@Getter
@Setter
@Accessors(chain = true)
public class SwaggerBean {
    private String swagger;
    private Info info;
    private String host;
    private String basePath;
    private List<Tags> tags;
    private List<String> schemes;
    private JSONObject paths;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    public static SwaggerBean convert(List<MethodInfo> methodInfos) {
        SwaggerBean swaggerBean = new SwaggerBean();
        swaggerBean.setSwagger("2.0");
        swaggerBean.setBasePath("/");
        swaggerBean.setSchemes(Arrays.asList("http", "https"));

        JSONObject paths = new JSONObject();
        methodInfos.forEach(methodInfo -> {
            final JSONObject child = new JSONObject();
            PathInfo pathInfo = new PathInfo();
            pathInfo.setTags(Lists.newArrayList());
            pathInfo.setSummary(OptionalUtil.ofBlank(methodInfo.getCommentDoc()).orElse(methodInfo.getMethodName()));
            pathInfo.setDescription(methodInfo.getDescription());
            pathInfo.setOperationId("");
            pathInfo.setConsumes(Lists.newArrayList("application/json"));
            pathInfo.setProduces(Lists.newArrayList("application/json"));

            Optional.ofNullable(methodInfo.getBodyParams())
                    .ifPresent(bodyParam -> {
                        Parameter parameter = Parameter.create("body", "body", bodyParam);
                        pathInfo.getParameters().add(parameter);
                    });

            OptionalUtil.ofEmpty(methodInfo.getQueryParams())
                    .orElse(Lists.newLinkedList())
                    .stream()
                    .map(queryParam -> Parameter.create("query",
                            queryParam.getName(),
                            queryParam.getMockerType().getTypeName(),
                            queryParam.getDesc(),
                            queryParam.getRequired()))
                    .forEach(pathInfo.getParameters()::add);

            OptionalUtil.ofEmpty(methodInfo.getPathParams())
                    .orElse(Lists.newLinkedList())
                    .stream()
                    .map(pathParam -> Parameter.create("path",
                            pathParam.getName(),
                            pathParam.getMockerType().getTypeName(),
                            pathParam.getDesc(),
                            pathParam.getRequired()))
                    .forEach(pathInfo.getParameters()::add);

            pathInfo.fillResponse(methodInfo.getResponse());

            child.put(methodInfo.getRequestMethodEnum().name().toLowerCase(), pathInfo);
            paths.put(methodInfo.getRequestUri(), child);
        });

        swaggerBean.setPaths(paths);
        return swaggerBean;
    }


    @Data
    @Accessors(chain = true)
    public static class PathInfo {
        private List<String> tags;
        private String summary;
        private String description;
        private String operationId;
        private List<String> consumes;
        private List<String> produces;
        private List<Parameter> parameters = new ArrayList<>();
        private Map<String, ResponseInfo> responses = new HashMap<>();

        public void fillResponse(MethodInfo.FieldItem response) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setDescription("");
            responseInfo.setSchema(Schema.create(response));
            responses.put("200", responseInfo);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class ResponseInfo {
        private String description;
        private JSONObject schema;
    }

    @Data
    @Accessors(chain = true)
    public static class Contact {
        private String email;
    }

    @Data
    @Accessors(chain = true)
    public static class License {
        private String name;
        private String url;
    }

    @Data
    @Accessors(chain = true)
    public static class Info {
        private String description;
        private String version;
        private String title;
        private String termsOfService;
        private Contact contact;
        private License license;

        public static Info create(String title) {
            final Info info = new Info();
            info.setTitle(title);
            info.setVersion("last");
            return info;
        }
    }


    @Data
    @Accessors(chain = true)
    public static class ExternalDocs {

        private String description;
        private String url;
    }

    @Data
    @Accessors(chain = true)
    public static class Tags {

        private String name;
        private String description;
        private ExternalDocs externalDocs;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Schema {

        public static JSONObject create(MethodInfo.FieldItem fieldItem) {
            return resolveProperties(fieldItem);
        }
    }

    private static JSONObject resolveProperties(MethodInfo.FieldItem fieldItem) {
        JSONObject property = new JSONObject();
        property.put("type", fieldItem.getMockerType().getTypeName());
        property.put("description", fieldItem.getDesc());
        property.put("required", fieldItem.getRequired());
        property.put("example", fieldItem.getExample());

        switch (fieldItem.getMockerType()) {
            case Object:
                Map<String, JSONObject> items = new LinkedHashMap<>();
                fieldItem.getChildren().forEach(child -> {
                    items.put(child.getName(), resolveProperties(child));
                });
                property.put("properties", items);
                break;
            case Array:
                final MethodInfo.FieldItem arrayChild = fieldItem.getChildren().get(0);
                property.put("items", resolveProperties(arrayChild));
                break;
            default:
        }
        return property;
    }

    @Data
    @Accessors(chain = true)
    public static class Parameter {

        private String in;
        private String name;
        private String type;
        private String description;
        private Boolean required;
        private JSONObject schema;
        private JSONObject items;

        public static Parameter create(String in, String name) {
            Parameter parameter = new Parameter();
            parameter.setIn(in);
            parameter.setName(name);
            return parameter;
        }

        public static Parameter create(String in, String name, String type, String description, boolean required) {
            Parameter parameter = new Parameter();
            parameter.setIn(in);
            parameter.setName(name);
            parameter.setType(type);
            parameter.setRequired(required);
            parameter.setDescription(description);
            return parameter;
        }

        public static Parameter create(String in, String name, MethodInfo.FieldItem data) {
            Parameter parameter = new Parameter();
            parameter.setIn(in);
            parameter.setName(name);
            parameter.setSchema(Schema.create(data));
            return parameter;
        }
    }

}


