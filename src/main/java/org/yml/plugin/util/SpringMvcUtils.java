package org.yml.plugin.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiReference;
import org.yml.plugin.constants.AnnotationsConstant;
import org.yml.plugin.enums.RequestMappingEnum;
import org.yml.plugin.enums.RequestMethodEnum;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yaml
 * @since 2020/12/30
 */
public class SpringMvcUtils {

    private static final Pattern URI_PATTERN = Pattern.compile("\"([^\"]*)\"");

    public static String resolveClassUri(PsiClass selectedClass) {
        PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(selectedClass, AnnotationsConstant.RequestMapping);
        if (psiAnnotation == null) {
            return "";
        }
        PsiNameValuePair[] psiNameValuePairs = psiAnnotation.getParameterList().getAttributes();
        if (psiNameValuePairs.length < 1) {
            return "";
        }
        final String literalValue = psiNameValuePairs[0].getLiteralValue();
        if (literalValue != null) {
            return literalValue;
        }
        return Optional.ofNullable(psiAnnotation.findAttributeValue("value"))
                .map(PsiElement::getReference)
                .map(PsiReference::resolve)
                .map(PsiElement::getText)
                .map(text -> text.split("="))
                .map(array -> array[array.length - 1].split(";")[0])
                .map(value -> value.replace("\"", "").trim())
                .orElse("");
    }

    public static RequestMethodEnum resolveRequestMethodEnum(RequestMappingEnum mappingEnum, PsiNameValuePair[] attributes) {
        final RequestMethodEnum requestMethodEnum = mappingEnum.getRequestMethodEnum();
        if (requestMethodEnum != null) {
            return requestMethodEnum;
        }
        return Arrays.stream(attributes)
                .filter(attribute -> "method".equals(attribute.getName()))
                .map(attribute -> RequestMethodEnum.resolve(attribute.getLiteralValue()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(RequestMethodEnum.GET);
    }

    public static String resolveMethodUri(PsiNameValuePair[] attributes) {
        return Arrays.stream(attributes)
                .filter(attribute -> "value".equals(attribute.getAttributeName()))
                .findFirst()
                .map(PsiNameValuePair::getValue)
                .map(PsiElement::getText)
                .map(URI_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(0))
                .map(uri->uri.replace("\"", ""))
                .map(uri -> {
                    if (uri.startsWith("/")) {
                        return uri;
                    } else {
                        return "/" + uri;
                    }
                })
                .orElse("");
    }
}
