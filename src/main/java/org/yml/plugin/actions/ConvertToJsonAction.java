package org.yml.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.yml.plugin.module.KV;
import org.yml.plugin.ui.ConvertToJsonDialog;
import org.yml.plugin.util.PsiFieldUtils;

import static org.yml.plugin.constants.Constants.ACTION_NAME_CONVERT_TO_JSON;

/**
 * @author yaml
 * @since 2020/12/23
 */
public class ConvertToJsonAction extends AbstractAction {

    public ConvertToJsonAction() {
        super(ACTION_NAME_CONVERT_TO_JSON, "Convert Bean To Json", null);
    }

    @Override
    protected void action(@NotNull AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        final PsiClass currentClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        Assert.assertNotNull("类不存在", currentClass);
        final KV<String, Object> fields = PsiFieldUtils.getFields(currentClass);
        Assert.assertTrue("当前类字段为空", MapUtils.isNotEmpty(fields));
        String json = fields.toPrettyJson();
        ConvertToJsonDialog convertToJsonDialog = ConvertToJsonDialog.instance(currentClass.getQualifiedName(), json);
        convertToJsonDialog.setVisible(true);
    }
}
