package org.yml.plugin.ui;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SeparatorFactory;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.yml.plugin.config.CodeGeneratorSettings;
import org.yml.plugin.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author yaml
 * @since 2021/3/17
 */
public class TemplatesSettingConfigurable implements Configurable {

    private static final String TEMPLATE_DESCRIPTION;


    static {
        String descriptionInfo = "";
        try {
            descriptionInfo = UrlUtil.loadText(TemplatesSettingConfigurable.class.getResource("/template/description/templateDescription.html"));
        } catch (IOException e) {
            NotificationUtils.error("template description load fail: " + e.getMessage());
        } finally {
            TEMPLATE_DESCRIPTION = descriptionInfo;
        }
    }

    private JPanel mainPanel;
    private JList<String> templateList;

    /**
     * ???????????????
     */
    private Editor editor;
    private final Project project;

    private final CodeGeneratorSettings.CodeGeneratorState codeGeneratorState;
    private Map<String, String> currTemplateContentMap;

    public static TemplatesSettingConfigurable getInstance(Project project) {
        return new TemplatesSettingConfigurable(project);
    }

    public TemplatesSettingConfigurable(Project project) {
        this.project = project;
        this.codeGeneratorState = CodeGeneratorSettings.getInstance(project).getState();
        this.currTemplateContentMap = new LinkedHashMap<>(codeGeneratorState.getTemplateContentMap());
    }

    private void intEditor(String content) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        if (editor != null) {
            editorFactory.releaseEditor(editor);
        }
        initEditor(null, content);
        // ??????????????????
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                final String text = editor.getDocument().getText();
                final String key = templateList.getSelectedValue();
                currTemplateContentMap.put(key, text);
            }
        }, ((EditorImpl) editor).getDisposable());
    }

    private void initEditor(@Nullable PsiFile file, String content) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document doc = createDocument(file, content);
        editor = editorFactory.createEditor(doc, project);

        EditorSettings editorSettings = editor.getSettings();
        // ??????????????????
        editorSettings.setVirtualSpace(false);
        // ????????????????????????????????????
        editorSettings.setLineMarkerAreaShown(false);
        // ??????????????????
        editorSettings.setIndentGuidesShown(false);
        // ????????????
        editorSettings.setLineNumbersShown(true);
        // ??????????????????
        editorSettings.setFoldingOutlineShown(true);
        // ???????????????????????????????????????
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(3);
        // ?????????????????????
        editorSettings.setCaretRowShown(false);

        EditorHighlighter editorHighlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, new LightVirtualFile("aaa.java.ft"));
        ((EditorEx) editor).setHighlighter(editorHighlighter);
    }

    @NotNull
    private Document createDocument(@Nullable PsiFile file, String content) {
        Document document = file != null ? PsiDocumentManager.getInstance(file.getProject()).getDocument(file) : null;
        return document != null ? document : EditorFactory.getInstance().createDocument(content);
    }

    private void refreshTemplateEditor() {
        final String key = templateList.getSelectedValue();
        final String content = currTemplateContentMap.getOrDefault(key, "");
        if (editor == null) {
            this.intEditor(content);
            return;
        }
        // ??????????????????
        WriteCommandAction.runWriteCommandAction(null, () -> this.editor.getDocument().setText(content));

    }

    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(JBUI.size(400, 300));
        JPanel templateListPanel = new JPanel(new BorderLayout());

        JPanel templateContentPanel = new JPanel(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());

        // ????????????
        JEditorPane editorPane = new JEditorPane();
        // html????????????
        editorPane.setEditorKit(UIUtil.getHTMLEditorKit());
        // ?????????
        editorPane.setEditable(false);
        editorPane.setText(TEMPLATE_DESCRIPTION);
        // ????????????
        JPanel descriptionPanel = new JPanel(new GridBagLayout());
        descriptionPanel.add(SeparatorFactory.createSeparator(IdeBundle.message("label.description"), null),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insetsBottom(2), 0, 0));
        descriptionPanel.add(ScrollPaneFactory.createScrollPane(editorPane),
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        JBUI.insetsTop(2), 0, 0));

        // ?????????
        Splitter centerPanelSplitter = new Splitter(true, 0.6F);
        centerPanelSplitter.setFirstComponent(templateContentPanel);
        centerPanelSplitter.setSecondComponent(descriptionPanel);
        centerPanel.add(centerPanelSplitter, BorderLayout.CENTER);

        // ?????????
        Splitter splitter = new Splitter(false, 0.2F);
        splitter.setFirstComponent(templateListPanel);
        splitter.setSecondComponent(centerPanel);
        mainPanel.add(splitter, BorderLayout.CENTER);

        templateList = new JBList<>();
        templateListPanel.add(templateList, BorderLayout.CENTER);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        currTemplateContentMap.forEach((key, value) -> listModel.addElement(key));
        templateList.setModel(listModel);
        templateList.setSelectedIndex(0);
        refreshTemplateEditor();
        templateContentPanel.add(editor.getComponent(), BorderLayout.CENTER);
        templateList.addListSelectionListener(e -> refreshTemplateEditor());
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        Map<String, String> oldData = this.codeGeneratorState.getTemplateContentMap();
        for (Map.Entry<String, String> entry : this.currTemplateContentMap.entrySet()) {
            if (!Objects.equals(entry.getValue(), oldData.get(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        this.codeGeneratorState.setTemplateContentMap(this.currTemplateContentMap);
    }

    @Override
    public void reset() {
        if (!isModified()) {
            return;
        }
        this.currTemplateContentMap = new LinkedHashMap<>(this.codeGeneratorState.getTemplateContentMap());
        refreshTemplateEditor();
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
        if (editor != null) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
        editor = null;
    }
}
