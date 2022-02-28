package org.yml.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.yml.plugin.context.ApplicationContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author yaml
 * @since 2021/2/25
 */
public interface FileUtils {

    /**
     * 提示创建目录
     */
    static VirtualFile createDirectoryIfMissing(VirtualFile baseDir, String savePath) {
        VirtualFile saveDir = VfsUtil.findRelativeFile(savePath, baseDir);
        if (saveDir != null) {
            return saveDir;
        }
        // 尝试创建目录
        String msg = String.format("Directory %s Not Found, Confirm Create?", savePath);
        if (MessageDialogBuilder.yesNo("", msg).isYes()) {
            return FileUtils.createChildDirectory(ApplicationContext.currentProject(), baseDir, savePath);
        }
        return null;
    }

    /**
     * 创建子目录
     *
     * @param project 文件对象
     * @param parent  父级目录
     * @param dirName 子目录
     * @return 目录对象
     */
    static VirtualFile createChildDirectory(Project project, VirtualFile parent, String dirName) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<VirtualFile>) () -> {
            try {
                return VfsUtil.createDirectoryIfMissing(parent, dirName);
            } catch (IOException e) {
                NotificationUtils.error("目录创建失败：" + dirName);
                e.printStackTrace();
                return null;
            }
        });
    }

    static VirtualFile createFile(String path, String fileName, String content) {
        final VirtualFile baseDir = ModuleUtils.currentModuleDirFile();
        final VirtualFile saveDir = createDirectoryIfMissing(baseDir, path.replace(baseDir.getPath(), ""));
        if (saveDir == null) {
            return null;
        }

        if (!fileName.contains(".")) {
            return createChildFile(saveDir, fileName.concat(".java"), content);
        }
        return createChildFile(saveDir, fileName, content);
    }

    /**
     * 创建子文件
     *
     * @param parent   父级目录
     * @param fileName 子文件名
     * @return 文件对象
     */
    static VirtualFile createChildFile(@NotNull VirtualFile parent, @NotNull String fileName, String content) {
        final VirtualFile child = parent.findChild(fileName);
        boolean overwrite = false;
        if (child != null) {
            String msg = String.format("文件 %s 已存在, 是否覆盖?", fileName);
            if (MessageDialogBuilder.yesNo("", msg).isYes()) {
                overwrite = true;
            }
        }
        return createChildFile(parent, fileName, content, overwrite);
    }

    /**
     * 创建子文件
     *
     * @param parent    父级目录
     * @param fileName  子文件名
     * @param content   文件内容
     * @param overwrite 是否覆盖
     * @return 文件对象
     */
    static VirtualFile createChildFile(@NotNull VirtualFile parent,
                                       @NotNull String fileName,
                                       String content,
                                       boolean overwrite) {
        final Project project = ApplicationContext.currentProject();
        return WriteCommandAction.writeCommandAction(project).compute(() -> {
            try {
                VirtualFile virtualFile = parent.findChild(fileName);
                if (virtualFile == null) {
                    virtualFile = parent.createChildData(new Object(), fileName);
                    virtualFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                } else if (overwrite) {
                    virtualFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                }
                ReformatUtils.reformatFile(project, virtualFile);
                FileDocumentManager.getInstance().saveAllDocuments();
                return virtualFile;
            } catch (IOException e) {
                NotificationUtils.error("文件创建失败：" + fileName);
                e.printStackTrace();
                return null;
            }
        });
    }
}
