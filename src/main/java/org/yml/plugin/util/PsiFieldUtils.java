package org.yml.plugin.util;

import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import org.yml.plugin.enums.JavaTypeEnum;
import org.yml.plugin.enums.ListTypeEnum;
import org.yml.plugin.module.KV;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yaml
 * @since 2021/1/6
 */
public final class PsiFieldUtils {

    public static KV<String, Object> getFields(PsiClass psiClass) {
        if (psiClass == null) {
            return KV.create();
        }
        KV<String, Object> kv = KV.create();
        for (PsiField field : psiClass.getAllFields()) {
            final boolean isStatic = Optional.ofNullable(field.getModifierList())
                    .map(l -> l.hasModifierProperty(PsiModifier.STATIC))
                    .orElse(false);
            if (isStatic) {
                continue;
            }
            PsiType type = field.getType();
            String name = field.getName();
            final String shortTypeName = type.getPresentableText();
            final Optional<JavaTypeEnum> optionalJavaTypeEnum = JavaTypeEnum.of(shortTypeName);
            // unit Type
            if (optionalJavaTypeEnum.isPresent()) {
                kv.put(name, optionalJavaTypeEnum.get().getDefaultValue());
                continue;
            }
            // array type
            if (type instanceof PsiArrayType) {
                PsiType deepType = type.getDeepComponentType();
                ArrayList<Object> list = new ArrayList<>();
                String deepTypeName = deepType.getPresentableText();
                final Optional<JavaTypeEnum> optional = JavaTypeEnum.of(deepTypeName);
                if (optional.isPresent()) {
                    list.add(optional.get().getDefaultValue());
                } else {
                    list.add(getFields(PsiUtil.resolveClassInType(deepType)));
                }
                kv.put(name, list);
                continue;
            }
            // list type
            final Optional<ListTypeEnum> optionalListTypeEnum = ListTypeEnum.of(shortTypeName);
            if (optionalListTypeEnum.isPresent()) {
                PsiType iterableType = PsiUtil.extractIterableTypeParameter(type, false);
                PsiClass iterableClass = PsiUtil.resolveClassInClassTypeOnly(iterableType);
                assert iterableClass != null;
                ArrayList<Object> list = new ArrayList<>();
                String classTypeName = iterableClass.getName();
                final Optional<JavaTypeEnum> optional = JavaTypeEnum.of(classTypeName);
                if (optional.isPresent()) {
                    list.add(optional.get().getDefaultValue());
                } else if (Objects.equals(iterableClass, psiClass)) {
                    list.add(KV.create());
                } else {
                    list.add(getFields(iterableClass));
                }
                kv.put(name, list);
                continue;
            }

            final PsiClass fieldTypeClass = PsiUtil.resolveClassInType(type);
            // enum type
            if (fieldTypeClass != null && fieldTypeClass.isEnum()) {
                kv.put(name, Objects.requireNonNull(((PsiClassReferenceType) type).resolve()).getFields()[0].getName());
                continue;
            }
            // class type
            if (Objects.equals(fieldTypeClass, psiClass)) {
                kv.put(name, KV.create());
                continue;
            }
            kv.put(name, getFields(fieldTypeClass));
        }
        return kv;
    }
}
