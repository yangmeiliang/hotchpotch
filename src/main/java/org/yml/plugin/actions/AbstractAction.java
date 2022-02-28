package org.yml.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.yml.plugin.context.ApplicationContext;
import org.yml.plugin.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author yaml
 * @since 2021/1/29
 */
public abstract class AbstractAction extends AnAction implements DumbAware {

    protected AbstractAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (inDevelopment()) {
            NotificationUtils.inDevelopment();
            return;
        }
        ApplicationContext.cacheDataContext(e.getDataContext());
        try {
            action(e);
        } catch (Exception ex) {
            ex.printStackTrace();
            NotificationUtils.error("错误信息:" + ex.getMessage());
        } finally {
            ApplicationContext.clear();
            finallyInvoke();
        }

    }

    protected abstract void action(@NotNull AnActionEvent e) throws Exception;

    protected boolean inDevelopment() {
        return false;
    }

    protected void finallyInvoke() {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

}
