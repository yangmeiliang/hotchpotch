package org.yml.plugin.module.sql;

import com.intellij.openapi.project.Project;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yml.plugin.config.CodeGeneratorSettings;
import org.yml.plugin.util.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author yaml
 * @since 2021/2/4
 */
@Getter
@Setter
public class TableInfo {
    private static final String JAVA_LANG = "java.lang";
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表备注
     */
    private String tableComment;
    /**
     * 主键列
     */
    private List<TableColumnInfo> pkColumns;
    /**
     * 所有列
     */
    private List<TableColumnInfo> columns = new ArrayList<>();

    private String entityName;
    private String entityPath;

    private String mapperName;
    private String mapperPath;

    private String daoName;
    private String daoPath;

    private String daoImplName;
    private String daoImplPath;

    private String baseMapperPackage;
    private String baseDaoPackage;
    private String baseDaoImplPackage;

    private String entityPackage;
    private String mapperPackage;
    private String daoPackage;
    private String daoImplPackage;

    private String serviceName;
    private String controllerName;
    private String servicePackage;
    private String controllerPackage;

    private final Project project;
    private final CodeGeneratorSettings.CodeGeneratorState codeGeneratorState;

    public TableInfo(Project project) {
        this.project = project;
        this.codeGeneratorState = CodeGeneratorSettings.getInstance(project).getState();
    }

    public String getEntityName() {
        return this.entityName.concat(codeGeneratorState.getEntityNameSuffix());
    }

    public String getMapperName() {
        return getEntityName().concat(codeGeneratorState.getMapperNameSuffix());
    }

    public String getDaoName() {
        return getEntityName().concat(codeGeneratorState.getDaoNameSuffix());
    }

    public String getDaoImplName() {
        return getEntityName().concat(codeGeneratorState.getDaoImplNameSuffix());
    }

    public String getEntityPackage() {
        return codeGeneratorState.getBasePackage().concat(".").concat(codeGeneratorState.getEntityPackageSuffix());
    }

    public String getMapperPackage() {
        return codeGeneratorState.getBasePackage().concat(".").concat(codeGeneratorState.getMapperPackageSuffix());
    }

    public String getDaoPackage() {
        return codeGeneratorState.getBasePackage().concat(".").concat(codeGeneratorState.getDaoPackageSuffix());
    }

    public String getDaoImplPackage() {
        return codeGeneratorState.getBasePackage().concat(".").concat(codeGeneratorState.getDaoImplPackageSuffix());
    }


    public String getBaseMapperPackage() {
        return getMapperPackage();
    }

    public String getBaseDaoPackage() {
        return getDaoPackage();
    }

    public String getBaseDaoImplPackage() {
        return getDaoImplPackage();
    }

    public String getServiceName() {
        return getEntityName().concat("Service");
    }

    public String getControllerName() {
        return getEntityName().concat("Controller");
    }

    public String getServicePackage() {
        return codeGeneratorState.getBasePackage().concat(".").concat("service");
    }

    public String getControllerPackage() {
        return codeGeneratorState.getBasePackage().concat(".").concat("controller");
    }

    /**
     * 需要导入的包
     */
    public Set<String> getImportList() {
        Set<String> importList = new TreeSet<>();
        this.columns.forEach(column -> {
            if (!column.getTypeFullName().startsWith(JAVA_LANG)) {
                importList.add(column.getTypeFullName());
            }
        });
        return importList;
    }

    public static TableInfo getInstance(Project project, String tableName, String tableComment) {
        TableInfo tableInfo = new TableInfo(project);
        tableInfo.setTableName(tableName);
        tableInfo.setEntityName(NameUtils.getClassName(tableName));
        tableInfo.setTableComment(tableComment);
        return tableInfo;
    }

    public void addColumn(String columnName,
                          String columnType,
                          String columnComment) {
        addColumn(columnName, columnType, columnComment, false);
    }

    public void addColumn(String columnName,
                          String columnType,
                          String columnComment,
                          boolean isPk) {
        final TableColumnInfo columnInfo = TableColumnInfo.getInstance(columnName, columnType, columnComment, isPk);
        this.columns.add(columnInfo);
    }


    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TableColumnInfo {
        /**
         * 字段名
         */
        private String name;
        /**
         * 字段名对应的java字段名
         */
        private String fieldName;
        /**
         * 字段备注
         */
        private String comment;
        /**
         * 字段类型
         */
        private String type;
        /**
         * 字段对应的java类型全名
         */
        private String typeFullName;
        /**
         * 字段对应的java类型简称
         */
        private String typeShortName;
        /**
         * 是否主键
         */
        private boolean pk;


        public static TableColumnInfo getInstance(String columnName,
                                                  String columnType,
                                                  String columnComment,
                                                  boolean isPk) {
            final SqlTypeEnum sqlTypeEnum = SqlTypeEnum.of(columnType);
            TableColumnInfo columnInfo = new TableColumnInfo();
            columnInfo.setName(columnName);
            columnInfo.setFieldName(NameUtils.toHump(columnName));
            columnInfo.setComment(columnComment);
            columnInfo.setType(columnType);
            columnInfo.setTypeFullName(sqlTypeEnum.getJavaTypeFullName());
            columnInfo.setTypeShortName(sqlTypeEnum.getJavaTypeShortName());
            columnInfo.setPk(isPk);
            return columnInfo;
        }
    }
}
