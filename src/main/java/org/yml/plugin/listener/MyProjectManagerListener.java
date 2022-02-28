package org.yml.plugin.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * @author yaml
 * @since 2021/9/4
 */
public class MyProjectManagerListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        System.out.println("projectOpened: " + project.getName());
        ProjectManagerListener.super.projectOpened(project);
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        System.out.println("projectClosed: " + project.getName());
        ProjectManagerListener.super.projectClosed(project);
    }

    @Override
    public void projectClosing(@NotNull Project project) {
        ProjectManagerListener.super.projectClosing(project);

    }

    @Override
    public void projectClosingBeforeSave(@NotNull Project project) {
        ProjectManagerListener.super.projectClosingBeforeSave(project);

    }
}
