package org.yml.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.DocumentAdapter;
import org.apache.commons.lang3.StringUtils;
import org.yml.plugin.config.ApiMockerSettings;
import org.yml.plugin.properties.ApiMockerProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author yaml
 * @since 2021/9/2
 */
public class ApiMockerSettingConfigurable implements Configurable {

    private JPanel mainPanel;
    private JTextField devHostTextField;
    private JTextField prdHostTextField;
    private JTextField projectTokenTextField;
    private JTextField userTokenTextField;
    private JTextField importTypeTextField;
    private JTextField uploadUrlTextField;
    private JTextField returnClassTextField;
    private JComboBox<String> projectNameComboBox;
    private JButton addProjectNameButton;
    private JButton deleteButton;

    private ApiMockerSettings apiMockerSettings;
    private final Project project;

    private static final Pattern PATTERN_API_MOCKER = Pattern.compile("api-mocker.properties");

    public static ApiMockerSettingConfigurable getInstance(Project project) {
        return new ApiMockerSettingConfigurable(project);
    }

    public ApiMockerSettingConfigurable(Project project) {
        this.project = project;
    }

    private void refreshPanelFieldValue() {
        String selectedItem = (String) projectNameComboBox.getSelectedItem();
        ApiMockerProperties config = apiMockerSettings.getConfig(selectedItem);
        refreshPanelFieldValue(config);
    }

    private void refreshPanelFieldValue(ApiMockerProperties apiMockerProperties) {
        if (apiMockerProperties == null) {
            apiMockerProperties = new ApiMockerProperties();
        }
        devHostTextField.setText(apiMockerProperties.getDevHost());
        prdHostTextField.setText(apiMockerProperties.getPrdHost());
        projectTokenTextField.setText(apiMockerProperties.getProjectToken());
        userTokenTextField.setText(apiMockerProperties.getUserToken());
        importTypeTextField.setText(apiMockerProperties.getImportType());
        uploadUrlTextField.setText(apiMockerProperties.getUploadUrl());
        returnClassTextField.setText(apiMockerProperties.getReturnClass());
    }

    private void addDocumentListener() {
        devHostTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setDevHost(devHostTextField.getText()));
            }
        });
        prdHostTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setPrdHost(prdHostTextField.getText()));
            }
        });
        projectTokenTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setProjectToken(projectTokenTextField.getText()));
            }
        });
        userTokenTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setUserToken(userTokenTextField.getText()));
            }
        });
        importTypeTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setImportType(importTypeTextField.getText()));
            }
        });
        uploadUrlTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setUploadUrl(uploadUrlTextField.getText()));
            }
        });
        returnClassTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String selectedItem = (String) projectNameComboBox.getSelectedItem();
                Optional.ofNullable(apiMockerSettings.getProjectConfigMap().get(selectedItem))
                        .ifPresent(config -> config.setReturnClass(returnClassTextField.getText()));
            }
        });
    }

    @Override
    public String getDisplayName() {
        return "ApiMocker Setting";
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        apiMockerSettings = ApiMockerSettings.getInstance().clone();
        // 配置字段修改监听器
        addDocumentListener();

        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        apiMockerSettings.getProjectConfigMap().forEach((key, value) -> comboBoxModel.addElement(key));
        projectNameComboBox.setModel(comboBoxModel);

        apiMockerSettings.getProjectConfigMap().keySet().stream()
                .max(Comparator.comparingInt(String::length))
                .map(String::length)
                .ifPresent(len -> projectNameComboBox.setPreferredSize(new Dimension(len * 10, 30)));

        // 默认展示第一个配置项
        refreshPanelFieldValue();

        addProjectNameButton.addActionListener(e -> {
            Messages.InputDialog dialog = new Messages.InputDialog(project, "请输入项目名（src的上一级文件名）：", "新增配置", null, "", null);
            dialog.show();
            if (dialog.isOK() && StringUtils.isNotBlank(dialog.getInputString()) && !apiMockerSettings.getProjectConfigMap().containsKey(dialog.getInputString())) {
                String inputString = dialog.getInputString();
                List<File> files = FileUtil.findFilesByMask(PATTERN_API_MOCKER, new File(project.getBasePath()));
                String path = files.stream().map(File::getPath).filter(p -> p.contains("/" + inputString +"/src/main/resources")).findFirst().orElse(project.getBasePath() + "/" + inputString);
                apiMockerSettings.getProjectConfigMap().putIfAbsent(inputString, ApiMockerProperties.getInstance(project, path));
                comboBoxModel.addElement(inputString);
                projectNameComboBox.setSelectedItem(inputString);
                refreshPanelFieldValue();
            }
        });

        projectNameComboBox.addItemListener(e -> refreshPanelFieldValue());

        deleteButton.addActionListener(e -> {
            String selectedItem = (String) projectNameComboBox.getSelectedItem();
            MessageDialogBuilder.YesNo yesNo = MessageDialogBuilder.yesNo("", String.format("delete config 「%s」?", selectedItem));
            if (yesNo.isYes()) {
                apiMockerSettings.getProjectConfigMap().remove(selectedItem);
                comboBoxModel.removeElement(selectedItem);
                refreshPanelFieldValue();
            }
        });
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !apiMockerSettings.equals(ApiMockerSettings.getInstance());
    }

    @Override
    public void apply() throws ConfigurationException {
        ApiMockerSettings.getInstance().overwrite(apiMockerSettings);
    }

    @Override
    public void reset() {
        if (!isModified()) {
            return;
        }
        this.apiMockerSettings.overwrite(ApiMockerSettings.getInstance());
    }

    @Override
    public void cancel() {
        onClose();
    }

    @Override
    public void disposeUIResources() {
        onClose();
    }

    private void onClose() {

    }
}
