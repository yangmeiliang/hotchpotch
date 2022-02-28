package org.yml.plugin.util;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.CodeCleanupCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.RearrangeCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeAction;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码格式化工具类
 *
 * @author yaml
 * @since 2021/2/23
 */
public class ReformatUtils {

    public static void reformatVirtualFile(Project project, List<VirtualFile> virtualFiles) {
        reformatVirtualFile(project, virtualFiles.toArray(new VirtualFile[0]));
    }

    public static void reformatVirtualFile(Project project, VirtualFile[] virtualFiles) {
        reformatFile(ReformatCodeAction.convertToPsiFiles(virtualFiles, project));
    }

    /**
     * 格式化虚拟文件
     *
     * @param project     项目对象
     * @param virtualFile 虚拟文件
     */
    public static void reformatFile(Project project, VirtualFile... virtualFile) {
        final List<PsiFile> psiFiles = Arrays.stream(virtualFile).map(PsiManager.getInstance(project)::findFile).collect(Collectors.toList());
        reformatFile(project, psiFiles);
    }

    public static void reformatFile(PsiFile... psiFiles) {
        reformatFile(psiFiles[0].getProject(), Arrays.asList(psiFiles));
    }

    public static void reformatFile(Project project, List<PsiFile> psiFiles) {
        if (CollectionUtils.isEmpty(psiFiles)) {
            return;
        }
        PsiDocumentManager.getInstance(project).commitAllDocuments();
        // 尝试对文件进行格式化处理
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(project, PsiUtilCore.toPsiFileArray(psiFiles), null, false);
        // 优化导入
        processor = new OptimizeImportsProcessor(processor);
        // 清理代码
        processor = new CodeCleanupCodeProcessor(processor);
        // 重新编排代码
        processor = new RearrangeCodeProcessor(processor);
        // 执行处理
        processor.run();
    }
}
