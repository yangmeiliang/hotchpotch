package org.yml.plugin.util;

import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;

import java.util.Objects;

/**
 * @author yaml
 * @since 2020/12/31
 */
public class PsiTypeUtils {

    private static final String LT = "<";
    private static final String SEPARATOR = ".";

    /**
     * 判断是否泛型
     */
    public static boolean isGenerics(PsiType psiType) {
        if (!(psiType instanceof PsiClassReferenceType)) {
            return false;
        }
        // 相等说明是泛型 例如 T R U 之类的标识
        return Objects.equals(psiType.getPresentableText(), psiType.getCanonicalText());
    }

    public static String extractFullName(String canonicalText) {
        if (!canonicalText.contains(LT)) {
            return canonicalText;
        }
        return canonicalText.substring(0, canonicalText.indexOf(LT));
    }

    public static String extractChildFullName(String canonicalText) {
        if (!canonicalText.contains(LT)) {
            return "";
        }
        return canonicalText.substring(canonicalText.indexOf(LT) + 1, canonicalText.lastIndexOf(">"));
    }

    public static String extractShortName(String fullName) {
        if (!fullName.contains(SEPARATOR) && !fullName.contains(LT)) {
            return fullName;
        }
        final int index = fullName.indexOf(LT);
        if (index != -1) {
            final String substring = fullName.substring(0, index);
            return substring.substring(substring.lastIndexOf(SEPARATOR) + 1);
        }
        return fullName.substring(fullName.lastIndexOf(SEPARATOR) + 1);
    }
}
