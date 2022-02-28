package org.yml.plugin.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.yml.plugin.util.NotificationUtils;

import static org.yml.plugin.constants.Constants.ACTION_NAME_RESTART;

/**
 * @author yaml
 * @since 2021/1/28
 */
public class RestartAction extends AbstractAction {

    public RestartAction() {
        super(ACTION_NAME_RESTART, "Restart IDE", AllIcons.Actions.Restart);
    }

    @Override
    protected void action(@NotNull AnActionEvent e) throws Exception {
        NotificationUtils.checkAndExpire(e);
        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().restart());
    }
}
