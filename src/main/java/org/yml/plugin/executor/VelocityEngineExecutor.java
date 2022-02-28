package org.yml.plugin.executor;

import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.yml.plugin.config.CodeGeneratorSettings;
import org.yml.plugin.context.ApplicationContext;
import org.yml.plugin.util.GlobalTool;
import org.yml.plugin.util.NameUtils;
import org.yml.plugin.util.NotificationUtils;
import org.yml.plugin.util.StringUtil;
import org.yml.plugin.util.VelocityUtils;
import org.junit.Assert;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author yaml
 * @since 2021/2/5
 */
@Slf4j
@Getter
@Setter
public class VelocityEngineExecutor {

    private Project project;
    private Template template;
    private String templateContent;

    /**
     * velocity配置
     */
    private Properties velocityProperties;
    private VelocityContext velocityContext;
    private Properties engineProperties;
    private VelocityEngine velocityEngine;

    /**
     * 最终生成文件内容
     */
    private String codeText;

    private VelocityEngineExecutor(Project project) {
        this.project = project;
        final Properties defaultProperties = FileTemplateManager.getDefaultInstance().getDefaultProperties();
        velocityProperties = new Properties();
        velocityProperties.putAll(defaultProperties);
        velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogChute");

        velocityContext = new VelocityContext();
        velocityContext.put("GlobalTool", GlobalTool.class);
        velocityContext.put("NameUtils", NameUtils.class);
        velocityContext.put("VelocityUtils", VelocityUtils.class);
        velocityContext.put("tableSaveInfo", CodeGeneratorSettings.getInstance(project).getState());

        Properties p = FileTemplateManager.getInstance(ApplicationContext.currentRequiredProject()).getDefaultProperties();
        for (Enumeration<?> e = p.propertyNames(); e.hasMoreElements(); ) {
            String s = (String) e.nextElement();
            velocityContext.put(s, p.getProperty(s));
        }
        velocityEngine = new VelocityEngine(velocityProperties);
    }

    public static VelocityEngineExecutor getInstance(Project project) {
        return new VelocityEngineExecutor(project);
    }

    /**
     * 创建classpath解析引擎
     */
    public VelocityEngineExecutor createClasspathEngine() {
        velocityEngine.setProperty("resource.loader", "classpath");
        velocityEngine.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return this;
    }

    /**
     * 创建文件路径解析引擎
     */
    public VelocityEngineExecutor createFileEngine(String templateDirPath) {
        velocityEngine.setProperty("resource.loader", "file");
        velocityEngine.setProperty("file.resource.loader.path", templateDirPath);
        return this;
    }

    public VelocityEngineExecutor templateContent(String templateContent) {
        this.templateContent = templateContent;
        return this;
    }

    public VelocityEngineExecutor contextData(String key, Object value) {
        this.velocityContext.put(key, value);
        return this;
    }

    public VelocityEngineExecutor execute() {
        try (StringWriter stringWriter = new StringWriter()) {
            velocityEngine.init();
            velocityEngine.evaluate(velocityContext, stringWriter, "", templateContent);
            codeText = stringWriter.toString();
        } catch (Exception e) {
            // 将异常全部捕获，直接返回，用于写入模板
            StringBuilder builder = new StringBuilder("在生成代码时，模板发生了如下语法错误：\n");
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
            codeText = builder.toString();
            NotificationUtils.error(String.format("模板语法错误：%s", e.getMessage()));
        }
        return this;
    }

    @SneakyThrows
    public PsiFile storeJavaFile(String path, String fileName) {
        try {
            return store(path, fileName);
        } catch (Exception e) {
            NotificationUtils.error(String.format("【%s】文件保存出错：%s", fileName, e.getMessage()));
        }
        return null;
    }

    @SneakyThrows
    public PsiFile store(String path, String fileName) {
        String finalContent = StringUtil.removeStart(codeText, "\n");
        String finalFileName = fileName.concat(JavaFileType.DOT_DEFAULT_EXTENSION);
        VirtualFile dir = WriteAction.compute(() -> VfsUtil.createDirectoryIfMissing(path));
        Assert.assertNotNull(String.format("目录创建失败：%s", path), dir);
        PsiDirectory psiDirectory = PsiDirectoryFactory.getInstance(project).createDirectory(dir);
        Assert.assertNotNull(String.format("目录创建失败：%s", path), psiDirectory);
        PsiFile file = psiDirectory.findFile(finalFileName);
        if (file != null) {
            String msg = String.format("文件 %s 已存在, 是否覆盖?", finalFileName);
            if (!MessageDialogBuilder.yesNo("", msg).isYes()) {
                return null;
            }
        }
        return WriteCommandAction.writeCommandAction(project).compute(() -> {
            if (file != null) {
                file.delete();
            }
            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(finalFileName, JavaLanguage.INSTANCE, finalContent);
            psiDirectory.add(psiFile);
            return psiFile;
        });

    }


}
