package org.yml.plugin.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.Consumer;
import org.yml.plugin.config.CodeGeneratorSettings;
import org.yml.plugin.executor.VelocityEngineExecutor;
import org.yml.plugin.module.sql.TableInfo;
import org.yml.plugin.util.ModuleUtils;
import org.yml.plugin.util.NameUtils;
import org.yml.plugin.util.NotificationUtils;
import org.yml.plugin.util.ReformatUtils;
import org.yml.plugin.util.VelocityUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yaml
 * @since 2021/3/2
 */
@SuppressWarnings("ALL")
public class CodeGeneratorDialog extends JDialog {

    public static final List<PsiFile> PSI_FILE_LIST = new ArrayList<>();
    private final Project project;
    private final LinkedHashMap<String, TableInfo> tableMap;
    private final CodeGeneratorSettings.CodeGeneratorState codeGeneratorState;
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private JPanel mainPanel;
    private JList<String> tableList;
    private JTextField projectPathTextField;
    private JTextField basePackageTextField;
    private JTextField basePathTextField;
    private JTextField entityNameSuffixTextField;
    private JTextField mapperNameSuffixTextField;
    private JTextField daoNameSuffixTextField;
    private JTextField daoImplNameSuffixTextField;
    private JTextField entityNameTextField;
    private JTextField entityPackageSuffixTextField;
    private JTextField mapperNameTextField;
    private JTextField mapperPackageSuffixTextField;
    private JTextField daoNameTextField;
    private JTextField daoPackageSuffixTextField;
    private JTextField daoImplNameTextField;
    private JTextField daoImplPackageSuffixTextField;
    private JTextField entityPathTextField;
    private JTextField mapperPathTextField;
    private JTextField daoPathTextField;
    private JTextField daoImplPathTextField;
    private JButton runButton;
    private JButton cancelButton;
    private JCheckBox entityCheckBox;
    private JCheckBox daoCheckBox;
    private JCheckBox mapperCheckBox;
    private JCheckBox daoImplCheckBox;
    private JCheckBox serviceCheckBox;
    private JCheckBox controllerCheckBox;
    private JCheckBox serviceTestCheckBox;
    private JTextField serviceNameTextField;
    private JTextField controllerNameTextField;
    private JTextField servicePathTextField;
    private JTextField controllerPathTextField;
    private JList<String> resultList;

    public CodeGeneratorDialog(Project project, LinkedHashMap<String, TableInfo> tableMap) {
        PSI_FILE_LIST.clear();
        this.project = project;
        this.tableMap = tableMap;
        this.codeGeneratorState = CodeGeneratorSettings.getInstance(project).getState();

        projectPathTextField.setText(ModuleUtils.currentModulePath());
        basePathTextField.setText(codeGeneratorState.getBasePath());
        basePackageTextField.setText(codeGeneratorState.getBasePackage());

        entityPackageSuffixTextField.setText(codeGeneratorState.getEntityPackageSuffix());
        mapperPackageSuffixTextField.setText(codeGeneratorState.getMapperPackageSuffix());
        daoPackageSuffixTextField.setText(codeGeneratorState.getDaoPackageSuffix());
        daoImplPackageSuffixTextField.setText(codeGeneratorState.getDaoImplPackageSuffix());

        entityNameSuffixTextField.setText(codeGeneratorState.getEntityNameSuffix());
        mapperNameSuffixTextField.setText(codeGeneratorState.getMapperNameSuffix());
        daoNameSuffixTextField.setText(codeGeneratorState.getDaoNameSuffix());
        daoImplNameSuffixTextField.setText(codeGeneratorState.getDaoImplNameSuffix());

        entityCheckBox.setSelected(true);
        mapperCheckBox.setSelected(true);
        daoCheckBox.setSelected(true);
        daoImplCheckBox.setSelected(true);
        serviceCheckBox.setSelected(false);
        controllerCheckBox.setSelected(false);
        serviceTestCheckBox.setSelected(false);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        this.tableMap.forEach((key, value) -> listModel.addElement(key));
        tableList.setModel(listModel);

        DefaultListModel<String> resultModel = new DefaultListModel<>();
        resultModel.addElement(String.format("table total size: %s", listModel.size()));
        resultList.setModel(resultModel);

        super.setContentPane(mainPanel);
        super.setModal(true);
        tableList.setSelectedIndex(0);
        handleTableSelection(tableMap);

        cancelButton.addActionListener(e -> onClose());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        mainPanel.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        runButton.addActionListener(e -> {
            runButton.setEnabled(false);
            printLog("code generate start...");
            codeGenerate(tableMap);
            printLog("code generate end...");
            runButton.setEnabled(true);
            onClose();
            ReformatUtils.reformatFile(project, PSI_FILE_LIST);
            NotificationUtils.info("code generate success!");
        });

        tableList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            List<String> selectedValuesList = tableList.getSelectedValuesList();
            if (selectedValuesList.size() > 1) {
                printLog(String.format("selected table size: %s", selectedValuesList.size()));
            }
            handleTableSelection(tableMap);
        });


        addTextChangeListener(basePathTextField, codeGeneratorState::setBasePath);
        addTextChangeListener(projectPathTextField, null);
        addTextChangeListener(basePackageTextField, codeGeneratorState::setBasePackage);
        addTextChangeListener(entityPackageSuffixTextField, codeGeneratorState::setEntityPackageSuffix);
        addTextChangeListener(mapperPackageSuffixTextField, codeGeneratorState::setMapperPackageSuffix);
        addTextChangeListener(daoPackageSuffixTextField, codeGeneratorState::setDaoPackageSuffix);
        addTextChangeListener(daoImplPackageSuffixTextField, codeGeneratorState::setDaoImplPackageSuffix);
        addTextChangeListener(entityNameSuffixTextField, codeGeneratorState::setEntityNameSuffix);
        addTextChangeListener(mapperNameSuffixTextField, codeGeneratorState::setMapperNameSuffix);
        addTextChangeListener(daoNameSuffixTextField, codeGeneratorState::setDaoNameSuffix);
        addTextChangeListener(daoImplNameSuffixTextField, codeGeneratorState::setDaoImplNameSuffix);

        entityNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                final String selectedValue = tableList.getSelectedValue();
                final TableInfo tableInfo = tableMap.get(selectedValue);
                tableInfo.setEntityName(entityNameTextField.getText());
                // 刷新表单数据
                handleTableSelection(tableMap);
            }
        });
    }

    public static void show(Project project, String title, LinkedHashMap<String, TableInfo> tableMap) {
        CodeGeneratorDialog dialog = new CodeGeneratorDialog(project, tableMap);
        dialog.pack();
        dialog.setTitle(title);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    protected AnActionEvent createEventFor(AnAction action) {
        VirtualFile[] files = PSI_FILE_LIST.stream().map(PsiFile::getVirtualFile).toArray(VirtualFile[]::new);
        return AnActionEvent.createFromAnAction(action, null, "", dataId -> {
            if (CommonDataKeys.VIRTUAL_FILE_ARRAY.is(dataId)) return files;
            if (CommonDataKeys.PROJECT.is(dataId)) return project;
            return null;
        });
    }

    private void codeGenerate(Map<String, TableInfo> tableMap) {
        List<String> selectTableNames = tableList.getSelectedValuesList();
        tableMap.forEach((tableName, tableInfo) -> {
            if (selectTableNames.size() > 1 && !selectTableNames.contains(tableName)) {
                return;
            }
            // 生成entity
            if (entityCheckBox.isSelected()) {
                String tempateName = "entity.java";
                String className = tableInfo.getEntityName();
                String path = entityPathTextField.getText();
                generateJavaFile(className, tempateName, path, tableInfo);
            }
            // 生成mapper
            if (mapperCheckBox.isSelected()) {
                String tempateName = "mapper.java";
                String className = tableInfo.getMapperName();
                String path = mapperPathTextField.getText();
                generateJavaFile(className, tempateName, path, tableInfo);
            }
            // 生成dao
            if (daoCheckBox.isSelected()) {
                String tempateName = "dao.java";
                String className = tableInfo.getDaoName();
                String path = daoPathTextField.getText();
                generateJavaFile(className, tempateName, path, tableInfo);
            }
            // 生成daoImpl
            if (daoImplCheckBox.isSelected()) {
                String tempateName = "daoImpl.java";
                String className = tableInfo.getDaoImplName();
                String path = daoImplPathTextField.getText();
                generateJavaFile(className, tempateName, path, tableInfo);
            }
            // service
            if (serviceCheckBox.isSelected()) {
                String tempateName = "service.java";
                String className = tableInfo.getServiceName();
                String path = servicePathTextField.getText();
                generateJavaFile(className, tempateName, path, tableInfo);
            }
            // controller
            if (controllerCheckBox.isSelected()) {
                String tempateName = "controller.java";
                String className = tableInfo.getControllerName();
                String path = controllerPathTextField.getText();
                generateJavaFile(className, tempateName, path, tableInfo);
            }
            // serviceTest
            if (serviceTestCheckBox.isSelected()) {
                String tempateName = "serviceTest.java";
                String className = tableInfo.getServiceName().concat("Test");
                String path = servicePathTextField.getText().replace("/src/main/java", "/src/test/java");
                generateJavaFile(className, tempateName, path, tableInfo);
            }
        });
    }

    private void addTextChangeListener(JTextField textField, Consumer<String> consumer) {
        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                Optional.ofNullable(consumer).ifPresent(c -> consumer.consume(textField.getText()));
                handleTableSelection(tableMap);
            }
        });
    }

    private void compareAndSet(JTextField textField, String value) {
        if (!Objects.equals(textField.getText(), value)) {
            textField.setText(value);
        }
    }

    private void handleTableSelection(Map<String, TableInfo> tableMap) {
        final String selectedValue = tableList.getSelectedValue();
        final TableInfo tableInfo = tableMap.get(selectedValue);

        compareAndSet(entityNameTextField, tableInfo.getEntityName());
        compareAndSet(mapperNameTextField, tableInfo.getMapperName());
        compareAndSet(daoNameTextField, tableInfo.getDaoName());
        compareAndSet(daoImplNameTextField, tableInfo.getDaoImplName());

        compareAndSet(entityPathTextField, generateFilePath(tableInfo.getEntityPackage()));
        compareAndSet(mapperPathTextField, generateFilePath(tableInfo.getMapperPackage()));
        compareAndSet(daoPathTextField, generateFilePath(tableInfo.getDaoPackage()));
        compareAndSet(daoImplPathTextField, generateFilePath(tableInfo.getDaoImplPackage()));

        compareAndSet(serviceNameTextField, tableInfo.getServiceName());
        compareAndSet(controllerNameTextField, tableInfo.getControllerName());
        compareAndSet(servicePathTextField, generateFilePath(tableInfo.getServicePackage()));
        compareAndSet(controllerPathTextField, generateFilePath(tableInfo.getControllerPackage()));
    }

    private String generateContent(String name) {
        return VelocityUtils.parseContent(name, codeGeneratorState.getTemplateContentMap());
    }

    private void generateJavaFile(String className, String templateName, String path, TableInfo tableInfo) {
        PsiFile psiFile = VelocityEngineExecutor.getInstance(tableInfo.getProject())
                .createClasspathEngine()
                .templateContent(generateContent(templateName))
                .contextData("tableInfo", tableInfo)
                .execute()
                .storeJavaFile(path, className);
        if (psiFile != null) {
            PSI_FILE_LIST.add(psiFile);
            printLog(String.format("%s 生成完成...", className));
        }
    }

    private void printLog(String content) {
        singleThreadExecutor.submit(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) resultList.getModel();
            model.add(0, content.concat("\n"));
            resultList.paintImmediately(resultList.getBounds());
        });
    }

    private String generateFilePath(String pkg) {
        return projectPathTextField.getText() + basePathTextField.getText() + NameUtils.parsePackage2Path(pkg);
    }


    private void onClose() {
        dispose();
    }
}
