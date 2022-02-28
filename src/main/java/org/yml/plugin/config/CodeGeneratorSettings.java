package org.yml.plugin.config;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Getter;
import lombok.Setter;
import org.yml.plugin.module.VmTemplate;
import org.yml.plugin.util.VelocityUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yaml
 * @since 2021/3/16
 */
@Getter
@Setter
@State(name = "CodeGeneratorSettings", storages = @Storage("HotchpotchCodeGeneratorSettings.xml"))
public class CodeGeneratorSettings implements PersistentStateComponent<CodeGeneratorSettings.CodeGeneratorState> {

    private final Project project;

    private final CodeGeneratorState state = new CodeGeneratorState();

    public CodeGeneratorSettings(Project project) {
        this.project = project;
    }

    public static CodeGeneratorSettings getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, CodeGeneratorSettings.class);
    }

    @Override
    public @NotNull CodeGeneratorState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull CodeGeneratorState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    @Override
    public void noStateLoaded() {

    }

    @Override
    public void initializeComponent() {

    }

    @Getter
    @Setter
    public static class CodeGeneratorState {
        public String basePackage = "cn.xxx.xxx";
        public String basePath = "/src/main/java";
        public String entityNameSuffix = "";
        public String mapperNameSuffix = "Mapper";
        public String daoNameSuffix = "Dao";
        public String daoImplNameSuffix = "DaoImpl";
        public String entityPackageSuffix = "entity";
        public String mapperPackageSuffix = "mapper";
        public String daoPackageSuffix = "dao";
        public String daoImplPackageSuffix = "dao.impl";
        public Map<String, String> templateContentMap = new LinkedHashMap<>(8);

        public CodeGeneratorState() {
            Lists.newArrayList(
                    VmTemplate.create("/template/common.vm"),
                    VmTemplate.create("/template/entity.java.vm"),
                    VmTemplate.create("/template/mapper.java.vm"),
                    VmTemplate.create("/template/dao.java.vm"),
                    VmTemplate.create("/template/daoImpl.java.vm"),
                    VmTemplate.create("/template/service.java.vm"),
                    VmTemplate.create("/template/controller.java.vm"),
                    VmTemplate.create("/template/serviceTest.java.vm")
            ).forEach(template -> templateContentMap.put(template.getName(), VelocityUtils.loadText(template.getPath())));
        }
    }
}
