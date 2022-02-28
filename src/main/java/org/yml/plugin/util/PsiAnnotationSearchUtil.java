package org.yml.plugin.util;

import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiQualifiedReference;
import com.intellij.psi.impl.source.SourceJavaCodeReference;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

import static org.yml.plugin.constants.AnnotationsConstant.RequestParam;

public class PsiAnnotationSearchUtil {
    private static final Key<String> LOMBOK_ANNOTATION_FQN_KEY = Key.create("LOMBOK_ANNOTATION_FQN");

    @Nullable
    public static PsiAnnotation findAnnotation(@NotNull PsiModifierListOwner psiModifierListOwner, @NotNull String annotationFQN) {
        return findAnnotationQuick(psiModifierListOwner.getModifierList(), annotationFQN);
    }

    @Nullable
    private static PsiAnnotation findAnnotationQuick(@Nullable PsiAnnotationOwner annotationOwner, @NotNull String annotationFQN) {
        if (annotationOwner == null) {
            return null;
        }

        PsiAnnotation[] annotations = annotationOwner.getAnnotations();
        if (annotations.length == 0) {
            return null;
        }

        final String shortName = StringUtil.getShortName(annotationFQN);

        for (PsiAnnotation annotation : annotations) {
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            if (null != referenceElement) {
                final String referenceName = referenceElement.getReferenceName();
                if (shortName.equals(referenceName)) {

                    //swagger注解为标注document 无法判定isQualified
                    if (referenceElement.isQualified() && referenceElement instanceof SourceJavaCodeReference) {
                        String possibleFullQualifiedName = ((SourceJavaCodeReference) referenceElement).getClassNameText();
                        if (annotationFQN.equals(possibleFullQualifiedName)) {
                            return annotation;
                        }
                    }

                    final String annotationQualifiedName = getAndCacheFQN(annotation, referenceName);
                    if (null != annotationQualifiedName && annotationFQN.endsWith(annotationQualifiedName)) {
                        return annotation;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private static String getAndCacheFQN(@NotNull PsiAnnotation annotation, @Nullable String referenceName) {
        String annotationQualifiedName = annotation.getCopyableUserData(LOMBOK_ANNOTATION_FQN_KEY);
        // if not cached or cache is not up to date (because existing annotation was renamed for example)
        if (null == annotationQualifiedName || (null != referenceName && !annotationQualifiedName.endsWith(".".concat(referenceName)))) {
            annotationQualifiedName = annotation.getQualifiedName();
            if (null != annotationQualifiedName && annotationQualifiedName.indexOf('.') > -1) {
                annotation.putCopyableUserData(LOMBOK_ANNOTATION_FQN_KEY, annotationQualifiedName);
            }
        }
        return annotationQualifiedName;
    }

    @NotNull
    public static String getSimpleNameOf(@NotNull PsiAnnotation psiAnnotation) {
        PsiJavaCodeReferenceElement referenceElement = psiAnnotation.getNameReferenceElement();
        return Optional.ofNullable(referenceElement).map(PsiQualifiedReference::getReferenceName).orElse("");
    }

    public static boolean checkAnnotationsSimpleNameExistsIn(@NotNull PsiModifierListOwner modifierListOwner, @NotNull Collection<String> annotationNames) {
        final PsiModifierList modifierList = modifierListOwner.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
            final String simpleName = getSimpleNameOf(psiAnnotation);
            if (annotationNames.contains(simpleName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkParamRequired(@NotNull PsiParameter psiParameter) {
        try {
            final PsiModifierList modifierList = psiParameter.getModifierList();
            if (modifierList == null) {
                return false;
            }
            for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                final String simpleName = getSimpleNameOf(psiAnnotation);
                if (RequestParam.equals(simpleName)) {
                    for (JvmAnnotationAttribute attribute : psiAnnotation.getAttributes()) {
                        if ("required".equals(attribute.getAttributeName())) {
                            JvmAnnotationAttributeValue attributeValue = attribute.getAttributeValue();
                            Boolean result = ReflecUtil.invoke(attributeValue, "getConstantValue");
                            return BooleanUtils.isTrue(result);
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (Exception ignore) {
            return false;
        }
    }

}
