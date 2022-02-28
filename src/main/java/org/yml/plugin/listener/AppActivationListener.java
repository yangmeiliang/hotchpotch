package org.yml.plugin.listener;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author yaml
 * @since 2021/7/8
 */
public class AppActivationListener implements ApplicationActivationListener {

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        ApplicationActivationListener.super.applicationActivated(ideFrame);
        System.out.println(ideFrame);
    }

    @Override
    public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
        ApplicationActivationListener.super.applicationDeactivated(ideFrame);
        System.out.println(ideFrame);
    }

    @Override
    public void delayedApplicationDeactivated(@NotNull Window ideFrame) {
        ApplicationActivationListener.super.delayedApplicationDeactivated(ideFrame);
        System.out.println(ideFrame);
    }
}
