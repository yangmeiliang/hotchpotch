package org.yml.plugin.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.yml.plugin.actions.UploadToApiMockerAction;
import org.yml.plugin.context.ApplicationContext;
import org.yml.plugin.enums.CollectionTypesEnum;
import org.yml.plugin.enums.NormalTypesEnum;
import org.yml.plugin.module.MethodInfo;
import org.yml.plugin.util.DescUtils;
import org.yml.plugin.util.PsiAnnotationSearchUtil;
import org.yml.plugin.util.PsiTypeUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.yml.plugin.constants.AnnotationsConstant.NotBlank;
import static org.yml.plugin.constants.AnnotationsConstant.NotEmpty;
import static org.yml.plugin.constants.AnnotationsConstant.NotNull;

/**
 * @author yaml
 * @since 2020/12/31
 */
public class FieldItemResolver {

    public static MethodInfo.FieldItem create(PsiParameter psiParameter) {
        MethodInfo.FieldItem fieldItem = resolve(psiParameter.getType());
        fieldItem.setName(psiParameter.getName());
        if (PsiAnnotationSearchUtil.checkParamRequired(psiParameter)) {
            fieldItem.setRequired(true);
        } else {
            fillRequired(fieldItem, psiParameter);
        }
        return fieldItem;
    }

    public static MethodInfo.FieldItem create(PsiType psiType) {
        return resolve(psiType);
    }

    /**
     * 解析方法返回对象
     *
     * @param returnWrapClass 包装类
     * @param returnType      包装类中的泛型数据类型
     */
    public static MethodInfo.FieldItem create(PsiClass returnWrapClass, PsiType returnType) {
        final String canonicalText = returnType.getCanonicalText();
        final boolean noWrap = returnWrapClass == null || canonicalText.startsWith(Objects.requireNonNull(returnWrapClass.getQualifiedName()));
        if (noWrap) {
            return resolve(returnType);
        }

        int size = returnWrapClass.getTypeParameters().length;
        Assert.assertEquals("包装类不支持，目前只支持一个泛型，且不能省略，例如：Result<T>", 1, size);
        final String fullName = returnWrapClass.getQualifiedName().concat("<").concat(canonicalText).concat(">");
        return resolve(fullName);
    }

    public static MethodInfo.FieldItem resolve(PsiType psiType) {
        return resolve(psiType.getCanonicalText());
    }

    public static MethodInfo.FieldItem resolve(String fullName) {
        MethodInfo.FieldItem fieldItem = new MethodInfo.FieldItem();
        fieldItem.setType(fullName);
        final String shortName = PsiTypeUtils.extractShortName(fullName);
        // 普通类型
        if (resolveIfIsNormalType(shortName, fieldItem)) {
            return fieldItem;
        }
        // 集合类型
        if (resolveIfIsCollect(fullName, shortName, fieldItem)) {
            return fieldItem;
        }
        // 泛型
        if (Objects.equals(fullName, shortName)) {
            return fieldItem;
        }
        // 防止循环嵌套导致栈溢出
        if (UploadToApiMockerAction.existClass(fullName)) {
            return fieldItem;
        }
        UploadToApiMockerAction.storeClass(fullName);

        // 其它类型
        final Project project = ApplicationContext.currentProject();
        assert project != null;

        final String classPath = PsiTypeUtils.extractFullName(fullName);
        final String childFullName = PsiTypeUtils.extractChildFullName(fullName);
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(classPath, GlobalSearchScope.allScope(project));
        Assert.assertNotNull("can not find class: " + classPath, psiClass);
        final String genericsTxt = Optional.of(psiClass.getTypeParameters())
                .filter(p -> p.length > 0)
                .map(p -> p[0])
                .map(PsiElement::getText).orElse("");
        Arrays.stream(psiClass.getAllFields()).map(field -> {
            if (Objects.requireNonNull(field.getModifierList()).hasModifierProperty(PsiModifier.STATIC)) {
                return null;
            }
            String fieldTypeFullName = field.getType().getCanonicalText();
            // 泛型字段替换真实路径
            if (fieldTypeFullName.contains(genericsTxt) && StringUtils.isNotBlank(childFullName)) {
                // 不相等说明是List<?>之类的形式
                fieldTypeFullName = Objects.equals(fieldTypeFullName, genericsTxt) ? childFullName : fieldTypeFullName.replace("<".concat(genericsTxt).concat(">"), "<" + childFullName + ">");
            }
            final MethodInfo.FieldItem item = resolve(fieldTypeFullName);
            fillFieldItem(item, field);
            return item;
        }).filter(Objects::nonNull).forEach(fieldItem.getChildren()::add);
        return fieldItem;
    }

    private static void fillRequired(MethodInfo.FieldItem item, PsiModifierListOwner modifierListOwner) {
        final boolean required = PsiAnnotationSearchUtil.checkAnnotationsSimpleNameExistsIn(modifierListOwner, Arrays.asList(NotBlank, NotNull, NotEmpty));
        item.setRequired(required);
    }

    private static void fillFieldItem(MethodInfo.FieldItem item, PsiField psiField) {
        final String fieldName = psiField.getName();
        final String filedDesc = DescUtils.getFieldDesc(psiField.getDocComment());
        final String filedMock = DescUtils.getFieldMockValue(psiField.getDocComment());
        item.setName(fieldName);
        item.setDesc(filedDesc);
        item.setExample(filedMock);
        fillRequired(item, psiField);
    }

    public static boolean resolveIfIsNormalType(String shortName, MethodInfo.FieldItem fieldItem) {
        final Optional<NormalTypesEnum> normalTypesEnumOptional = NormalTypesEnum.of(shortName);
        if (normalTypesEnumOptional.isPresent()) {
            fieldItem.setExample(normalTypesEnumOptional.get().getDefaultValue().toString());
            fieldItem.setMockerType(normalTypesEnumOptional.get().getMockerType());
            return true;
        }
        return false;
    }

    private static boolean resolveIfIsCollect(String fullName, String shortName, MethodInfo.FieldItem fieldItem) {
        final Optional<CollectionTypesEnum> collectionTypesEnumOptional = CollectionTypesEnum.of(shortName);
        if (!collectionTypesEnumOptional.isPresent()) {
            return false;
        }
        fieldItem.setMockerType(collectionTypesEnumOptional.get().getMockerType());
        switch (collectionTypesEnumOptional.get()) {
            case SET:
            case LINKED_HASH_SET:
            case LIST:
            case ARRAY_LIST:
            case LINKED_LIST:
            case COLLECTION:
                final String childFullName = PsiTypeUtils.extractChildFullName(fullName);
                MethodInfo.FieldItem child = resolve(childFullName);
                fieldItem.getChildren().add(child);
                break;
            case ARRAY_INTEGER:
            case ARRAY_STRING:
                fieldItem.getChildren().add(resolve(shortName.replace("[]", "")));
                break;
            case MAP:
            case HASH_MAP:
            case LINKED_HASH_MAP:
            default:
        }
        return true;
    }
}
