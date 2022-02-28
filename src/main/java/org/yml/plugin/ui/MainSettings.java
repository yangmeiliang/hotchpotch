package org.yml.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.yml.plugin.HotchpotchBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yaml
 * @since 2021/2/6
 */
public class MainSettings implements SearchableConfigurable.Parent {

    private final Configurable myConfigurable;
    private final List<Configurable> children;

    public MainSettings(Project project) {
        children = new ArrayList<>();
        if (!project.isDefault()) {
            children.add(TemplatesSettingConfigurable.getInstance(project));
            children.add(ApiMockerSettingConfigurable.getInstance(project));
        }
        myConfigurable = new MainSettings.MainSettingsConfigurable(project);
    }

    @Override
    public String getDisplayName() {
        return HotchpotchBundle.message("configurable.hotchpotch.display.name");
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        return myConfigurable.createComponent();
    }

    @Override
    public boolean isModified() {
        return myConfigurable.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        myConfigurable.apply();
    }

    @Override
    public @Nullable String getHelpTopic() {
        return myConfigurable.getHelpTopic();
    }

    @Override
    public @NotNull
    String getId() {
        return "Hotchpotch.Settings";
    }

    @Override
    public Configurable @NotNull [] getConfigurables() {
        return children.toArray(new Configurable[0]);
    }

    public static class MainSettingsConfigurable implements Configurable {

        public MainSettingsConfigurable(Project project) {

        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
            return null;
        }

        @Override
        public @Nullable JComponent createComponent() {
            return null;
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public void apply() throws ConfigurationException {

        }

        @Override
        public @Nullable String getHelpTopic() {
            return Configurable.super.getHelpTopic();
        }
    }
}
