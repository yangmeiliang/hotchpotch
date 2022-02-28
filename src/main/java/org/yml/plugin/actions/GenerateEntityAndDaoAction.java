package org.yml.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yml.plugin.module.sql.SqlTypeEnum;
import org.yml.plugin.module.sql.TableInfo;
import org.yml.plugin.ui.CodeGeneratorDialog;
import org.yml.plugin.util.PatternUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.yml.plugin.constants.Constants.ACTION_NAME_CODE_GENERATE;

/**
 * 数据库脚本自动生成实体类和DAO等模板类
 *
 * @author yaml
 * @since 2020/12/28
 */
@Slf4j
public class GenerateEntityAndDaoAction extends AbstractAction {

    static final Pattern PATTERN_TABLE_NAME = Pattern.compile("(?<=create\\s{1,100}table\\s{1,100}`?)\\w+(?=(`)?)", Pattern.CASE_INSENSITIVE);
    static final Pattern PATTERN_TABLE_COMMENT = Pattern.compile("(?<=comment\\s{0,10}=?\\s{0,10}').*(?=')", Pattern.CASE_INSENSITIVE);
    static final Pattern PATTERN_COLUMN_NAME = Pattern.compile("(?<=\\s?`?)\\w+(?=`?\\s?)", Pattern.CASE_INSENSITIVE);
    static final Pattern PATTERN_COLUMN_TYPE = Pattern.compile(SqlTypeEnum.allRegex(), Pattern.CASE_INSENSITIVE);
    static final Pattern PATTERN_COLUMN_COMMENT = Pattern.compile("(?<=comment\\s{1,100}').*(?=')", Pattern.CASE_INSENSITIVE);
    static final Pattern PATTERN_PRIMARY_KEY = Pattern.compile("(?<=PRIMARY\\s{1,100}KEY\\s{1,100}\\(`?)\\w+(?=`?\\))", Pattern.CASE_INSENSITIVE);
    static final Pattern PATTERN_CONTAINS_PRIMARY_KEY = Pattern.compile("PRIMARY\\s{1,100}KEY\\s*", Pattern.CASE_INSENSITIVE);

    protected GenerateEntityAndDaoAction() {
        super(ACTION_NAME_CODE_GENERATE, "Generate Entity、Mapper、Dao...", null);
    }


    @Override
    protected boolean inDevelopment() {
        return false;
    }

    @Override
    public void action(@NotNull AnActionEvent e) {
        final Editor editor = e.getRequiredData(LangDataKeys.EDITOR);
        final Project project = e.getRequiredData(LangDataKeys.PROJECT);
        String elementText = editor.getDocument().getText();

        String selectedTableName = editor.getCaretModel().getPrimaryCaret().getSelectedText();

        elementText = removeCommentLine(elementText);
        elementText = elementText.replaceAll("create\\s{1,100}table", "CREATE TABLE");

        LinkedHashMap<String, TableInfo> tableInfoMap = Arrays.stream(elementText.split("CREATE\\s{1,100}TABLE"))
                .filter(StringUtils::isNotBlank)
                .map("CREATE TABLE "::concat)
                .filter(ddl -> PATTERN_TABLE_NAME.matcher(ddl).find())
                .map(o -> resolveTableInfo(project, o))
                .filter(tableInfo -> {
                    if (StringUtils.isBlank(tableInfo.getTableName())) {
                        return false;
                    }
                    return StringUtils.isBlank(selectedTableName) || selectedTableName.equals(tableInfo.getTableName());
                })
                .collect(Collectors.toMap(TableInfo::getTableName, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));

        CodeGeneratorDialog.show(project, "code generator", tableInfoMap);
    }

    private String removeCommentLine(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        return Arrays.stream(text.split("\n"))
                .filter(StringUtils::isNotBlank)
                .filter(line -> !line.startsWith("#"))
                .filter(line -> !line.startsWith("-"))
                .collect(Collectors.joining("\n"));
    }

    private TableInfo resolveTableInfo(Project project, String selectedText) {
        // 第一部分 包含: 建表语句
        final String part1 = selectedText.substring(0, selectedText.indexOf("("));
        // 第二部分 包含: 表字段定义
        final String part2 = selectedText.substring(selectedText.indexOf("(") + 1, selectedText.lastIndexOf(")"));
        // 第三部分 包含 ENGINE、CHARSET、表的COMMENT
        final String part3 = selectedText.substring(selectedText.lastIndexOf(")") + 1);


        final String tableName = PatternUtils.groupFirst(PATTERN_TABLE_NAME, part1);
        final String tableComment = PatternUtils.groupFirst(PATTERN_TABLE_COMMENT, part3);
        final String pkName = PatternUtils.groupFirst(PATTERN_PRIMARY_KEY, part2);

        TableInfo tableInfo = TableInfo.getInstance(project, tableName, tableComment);
        Arrays.stream(part2.split(",\n"))
                .filter(line -> PATTERN_COLUMN_TYPE.matcher(line).find())
                .forEach(line -> {
                    final String name = PatternUtils.groupFirst(PATTERN_COLUMN_NAME, line);
                    final String type = PatternUtils.groupFirst(PATTERN_COLUMN_TYPE, line);
                    final String comment = PatternUtils.groupFirst(PATTERN_COLUMN_COMMENT, line);
                    final boolean isPk = PATTERN_CONTAINS_PRIMARY_KEY.matcher(line).find() || name.equals(pkName);
                    tableInfo.addColumn(name, type, comment, isPk);
                });
        return tableInfo;
    }

}
