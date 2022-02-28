package org.yml.plugin.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.yml.plugin.context.ApplicationContext;
import org.junit.Assert;

/**
 * @author yaml
 * @since 2021/2/5
 */
@SuppressWarnings("ALL")
public interface ModuleUtils {

    static String currentModulePath() {
        return currentModuleDirFile().getPath();
    }

    static VirtualFile currentModuleDirFile() {
        final Module module = ApplicationContext.currentModule();
        Assert.assertNotNull("请在具体的module下执行此操作", module);
        // 兼容老版本，新版本使用：final VirtualFile baseDir = ProjectUtil.guessModuleDir(module);
        final String moduleName = module.getName();
        VirtualFile baseDir = VfsUtil.findRelativeFile(module.getProject().getBaseDir(), moduleName);
        if (baseDir == null) {
            baseDir = module.getProject().getBaseDir();
        }
        Assert.assertNotNull("请在具体的module下执行此操作", baseDir);
        return baseDir;
    }

}
