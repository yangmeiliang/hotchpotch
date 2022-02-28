package org.yml.plugin.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yaml
 * @since 2021/1/4
 */
public class DescUtils {

    /**
     * 获取字段mock值
     */
    public static String getFieldMockValue(PsiDocComment psiDocComment) {
        return getDescLines(psiDocComment).stream()
                .filter(text -> text.contains("e.g. "))
                .findFirst()
                .map(v -> v.substring(v.indexOf("e.g.") + 4))
                .map(String::trim)
                .orElse(null);
    }

    /**
     * 获取字段注释
     */
    public static String getFieldDesc(PsiDocComment psiDocComment) {
        return getDescLines(psiDocComment).stream()
                .filter(text -> !text.startsWith("@"))
                .filter(text -> !text.startsWith("{"))
                .filter(text -> !text.startsWith("http"))
                .filter(text -> !text.startsWith("mock:"))
                .filter(text -> !text.startsWith("mock："))
                .filter(text -> !text.startsWith("e.g."))
                .collect(Collectors.joining(", "));
    }

    /**
     * 获取注释第一行
     */
    public static String getFirstLineDesc(PsiDocComment psiDocComment) {
        return getDescLines(psiDocComment).stream()
                .filter(text -> !text.startsWith("@"))
                .filter(text -> !text.startsWith("{"))
                .filter(text -> !text.startsWith("http"))
                .filter(text -> !text.startsWith("mock:"))
                .filter(text -> !text.startsWith("mock："))
                .findFirst()
                .orElse("");
    }

    /**
     * 获取方法注释
     */
    public static String getDescription(PsiDocComment psiDocComment) {
        return getDescLines(psiDocComment).stream()
                .filter(text -> !text.startsWith("@"))
                .filter(text -> !text.startsWith("{"))
                .filter(text -> !text.startsWith("http"))
                .filter(text -> !text.startsWith("mock:"))
                .filter(text -> !text.startsWith("mock："))
                .skip(1)
                .collect(Collectors.joining(""));
    }

    /**
     * 获取方法上的 @param 参数说明
     */
    public static Map<String, String> mapParamDesc(PsiDocComment psiDocComment) {
        if (psiDocComment == null) {
            return Collections.emptyMap();
        }
        return Arrays.stream(psiDocComment.getTags())
                .filter(tag -> "param".equals(tag.getName()))
                .map(PsiDocTag::getDataElements)
                .map(elements -> Arrays.stream(elements).map(PsiElement::getText).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList()))
                .filter(list -> list.size() > 1)
                .collect(Collectors.toMap(o -> o.get(0), o -> StringUtils.join(o.stream().skip(1).collect(Collectors.toList()), "\n")));
    }

    /**
     * 获取注释
     */
    public static List<String> getDescLines(PsiDocComment psiDocComment) {

        if (Objects.isNull(psiDocComment) || StringUtils.isBlank(psiDocComment.getText())) {
            return Collections.emptyList();
        }
        final PsiElement[] descriptionElements = psiDocComment.getDescriptionElements();
        return Arrays.stream(descriptionElements)
                .map(PsiElement::getText)
                .map(text -> text.replace("\n", ""))
                .map(text -> text.replace("\t", ""))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }
}
