package org.yml.plugin.module;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.yml.plugin.enums.MockerTypeEnum;
import org.yml.plugin.enums.RequestMethodEnum;

import java.util.LinkedList;

/**
 * @author yaml
 * @since 2020/12/29
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodInfo {

    /**
     * 方法备注
     */
    private String commentDoc;
    /**
     * 接口说明
     */
    private String description;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 请求uri
     */
    private String requestUri;
    /**
     * 请求方式
     */
    private RequestMethodEnum requestMethodEnum;
    /**
     * 请求路径中的参数
     */
    private LinkedList<FieldItem> pathParams;
    /**
     * 请求路径后面带的参数
     */
    private LinkedList<FieldItem> queryParams;
    /**
     * requestBody中的参数
     */
    private FieldItem bodyParams;
    /**
     * 返回对象
     */
    private FieldItem response;

    public static MethodInfo create(String methodName) {
        return new MethodInfo().setMethodName(methodName);
    }

    @Data
    public static class FieldItem {
        /**
         * 是否必填
         */
        private Boolean required = false;
        /**
         * 参数名字
         */
        private String name;
        /**
         * java类型
         */
        private String type;
        /**
         * mocker类型
         */
        private MockerTypeEnum mockerType = MockerTypeEnum.Object;
        /**
         * 描述
         */
        private String desc;
        /**
         * 示例
         */
        private String example;
        /**
         * MockerTypeEnum.Object：对象中的字段
         * MockerTypeEnum.Array：集合中的泛型对象
         * 其它情况 children为空
         */
        private LinkedList<FieldItem> children = Lists.newLinkedList();

        public static FieldItem create(String fieldName) {
            FieldItem fieldItem = new FieldItem();
            fieldItem.setName(fieldName);
            return fieldItem;
        }

        public FieldItem addChild() {
            return addChild("");
        }

        public FieldItem addChild(String fieldName) {
            return addChild(fieldName, "");
        }

        public FieldItem addChild(String fieldName, String fieldDesc) {
            return addChild(fieldName, fieldDesc, false);
        }

        public FieldItem addChild(String fieldName, String fieldDesc, boolean required) {
            final FieldItem fieldItem = new FieldItem();
            fieldItem.setName(fieldName);
            fieldItem.setDesc(fieldDesc);
            fieldItem.setRequired(required);
            children.add(fieldItem);
            return fieldItem;
        }
    }
}
