package org.yml.plugin.listener;

import com.intellij.ide.plugins.CannotUnloadPluginException;
import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import org.jetbrains.annotations.NotNull;

import static org.yml.plugin.constants.Constants.PLUGIN_ID_STR;

/**
 * @author yaml
 * @since 2022/2/28
 */
public class HotchpotchUpdateListener implements DynamicPluginListener {

    @Override
    public void beforePluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        DynamicPluginListener.super.beforePluginLoaded(pluginDescriptor);
        if (!isSelf(pluginDescriptor)) {
            return;
        }
    }

    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        DynamicPluginListener.super.beforePluginUnload(pluginDescriptor, isUpdate);
    }

    @Override
    public void checkUnloadPlugin(@NotNull IdeaPluginDescriptor pluginDescriptor) throws CannotUnloadPluginException {
        DynamicPluginListener.super.checkUnloadPlugin(pluginDescriptor);
    }

    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        DynamicPluginListener.super.pluginLoaded(pluginDescriptor);
    }

    @Override
    public void pluginUnloaded(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        DynamicPluginListener.super.pluginUnloaded(pluginDescriptor, isUpdate);
    }

    public boolean isSelf(IdeaPluginDescriptor pluginDescriptor) {
        return PLUGIN_ID_STR.equals(pluginDescriptor.getPluginId().toString());
    }
}
