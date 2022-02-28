package org.yml.plugin.util;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;
import org.yml.plugin.context.ApplicationContext;

import java.util.Optional;

import static org.yml.plugin.constants.Constants.NOTIFICATION_DISPLAY_ID;

/**
 * @author yaml
 * @since 2020/12/28
 */
public class NotificationUtils {

    private static final NotificationGroup NOTIFICATION_GROUP;

    static {
        NOTIFICATION_GROUP = new NotificationGroup(NOTIFICATION_DISPLAY_ID, NotificationDisplayType.BALLOON, true, null, AllIcons.General.Reset);
    }

    public static void checkAndExpire(AnActionEvent e) {
        DataKey<Notification> notificationKey = DataKey.create("Notification");
        Notification notification = notificationKey.getData(e.getDataContext());
        if (null != notification) {
            notification.expire();
        }
    }

    public static void error(String message) {
        notify(NotificationType.ERROR, "", message);
    }

    public static void info(String message) {
        notify(NotificationType.INFORMATION, "", message);
    }

    public static void notify(NotificationType type,
                              String title,
                              String message) {

        notify(null, null, type, title, message);
    }

    public static void notify(AnAction action,
                              NotificationType type,
                              String message) {

        notify(null, action, type, "", message);
    }

    public static void notify(@Nullable Project project,
                              @Nullable AnAction action,
                              NotificationType type,
                              String title,
                              String message) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(title, message, type, NotificationListener.URL_OPENING_LISTENER);
        Optional.ofNullable(action).ifPresent(notification::addAction);
        Notifications.Bus.notify(notification, project == null ? ApplicationContext.currentProject() : project);
    }

    public static void inDevelopment() {
        Messages.showMessageDialog("功能正在开发中，敬请期待!", "Sorry:", Messages.getWarningIcon());
    }
}
