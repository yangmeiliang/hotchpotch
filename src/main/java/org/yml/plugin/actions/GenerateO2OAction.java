package org.yml.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.CollectionListModel;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.List;

import static org.yml.plugin.constants.Constants.ACTION_NAME_GENERATE_O2O;

/**
 * @author yaml
 * @since 2020/12/23
 */
public class GenerateO2OAction extends AbstractAction {

    protected GenerateO2OAction() {
        super(ACTION_NAME_GENERATE_O2O, "convert one bean to another bean", null);
    }

    @Override
    protected void action(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        final PsiClass psiClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        Assert.assertNotNull("class not found", psiClass);
        final PsiMethod psiMethod = PsiTreeUtil.getParentOfType(referenceAt, PsiMethod.class);
        Assert.assertNotNull("method not found", psiMethod);
        // 启动写线程
        WriteCommandAction.writeCommandAction(psiMethod.getProject(), psiMethod.getContainingFile()).run(() -> createO2O(psiClass, psiMethod));
    }

    private void createO2O(final PsiClass psiClass, PsiMethod psiMethod) {
        String methodName = psiMethod.getName();
        PsiType returnType = psiMethod.getReturnType();
        if (returnType == null) {
            return;
        }
        String returnClassName = returnType.getPresentableText();
        PsiParameter psiParameter = psiMethod.getParameterList().getParameters()[0];
        // 带package的class名称
        String parameterClassWithPackage = psiParameter.getType().getInternalCanonicalText();
        // 为了解析字段，这里需要加载参数的class
        JavaPsiFacade facade = JavaPsiFacade.getInstance(psiMethod.getProject());
        PsiClass paramPsiClass = facade.findClass(parameterClassWithPackage, GlobalSearchScope.allScope(psiMethod.getProject()));
        if (paramPsiClass == null) {
            return;
        }
        List<PsiField> psiFields = new CollectionListModel<>(paramPsiClass.getFields()).getItems();
        String methodText = getMethodText(methodName, returnClassName, psiParameter, psiFields);
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        PsiMethod toMethod = elementFactory.createMethodFromText(methodText, psiMethod);
        psiMethod.replace(toMethod);
    }

    /**
     * @param methodName      方法名称
     * @param returnClassName 返回的值的class名称
     * @param psiParameter    方法参数第一个值
     * @param psiFields       方法参数的class里field 列表
     * @return 方法体的字符串
     */
    private String getMethodText(String methodName, String returnClassName, PsiParameter psiParameter,
                                 List<PsiField> psiFields) {
        String returnObjName = returnClassName.substring(0, 1).toLowerCase() + returnClassName.substring(1);
        String parameterClass = psiParameter.getText();
        String parameterName = psiParameter.getName();
        StringBuilder builder = new StringBuilder("public static " + returnClassName + " " + methodName + " (");
        builder.append(parameterClass).append(" ) {\n");
        builder.append("if ( ")
                .append(parameterName)
                .append("== null ){\n")
                .append("return null;\n}")
                .append(returnClassName)
                .append(" ")
                .append(returnObjName)
                .append("= new ")
                .append(returnClassName)
                .append("();\n");
        for (PsiField field : psiFields) {
            PsiModifierList modifierList = field.getModifierList();
            if (modifierList == null
                    || modifierList.hasModifierProperty(PsiModifier.STATIC)
                    || modifierList.hasModifierProperty(PsiModifier.FINAL)
                    || modifierList.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
                continue;
            }
            builder.append(returnObjName)
                    .append(".set")
                    .append(getFirstUpperCase(field.getName()))
                    .append("(")
                    .append(parameterName)
                    .append(".get")
                    .append(getFirstUpperCase(field.getName()))
                    .append("());\n");
        }
        builder.append("return ").append(returnObjName).append(";\n");
        builder.append("}\n");
        return builder.toString();
    }

    private String getFirstUpperCase(String oldStr) {
        return oldStr.substring(0, 1).toUpperCase() + oldStr.substring(1);
    }

    private PsiMethod getPsiMethodFromContext(AnActionEvent e) {
        PsiElement elementAt = getPsiElement(e);
        if (elementAt == null) {
            return null;
        }
        return PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
    }

    private PsiElement getPsiElement(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            e.getPresentation().setEnabled(false);
            return null;
        }
        // 用来获取当前光标处的PsiElement
        int offset = editor.getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }
}
