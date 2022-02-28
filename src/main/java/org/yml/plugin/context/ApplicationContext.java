package org.yml.plugin.context;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author yaml
 * @since 2021/1/29
 */
public class ApplicationContext {

    private static final ThreadLocal<DataContext> DATA_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static @Nullable
    Project currentProject() {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        return currentDataContext().map(o -> o.getData(LangDataKeys.PROJECT)).orElse(openProjects[0]);
    }

    public static @NotNull
    Project currentRequiredProject() {
        return Objects.requireNonNull(currentProject());
    }

    public static @Nullable
    Module currentModule() {
        return currentDataContext().map(o -> o.getData(LangDataKeys.MODULE)).orElse(null);
    }

    public static Optional<DataContext> currentDataContext() {
        return Optional.ofNullable(DATA_CONTEXT_THREAD_LOCAL.get());
    }

    public static void cacheDataContext(DataContext dataContext) {
        DATA_CONTEXT_THREAD_LOCAL.set(dataContext);
    }

    public static void clear() {
        DATA_CONTEXT_THREAD_LOCAL.remove();
    }


}
