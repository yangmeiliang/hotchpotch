package org.yml.plugin.actions;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.yml.plugin.HotchpotchBundle;
import org.yml.plugin.config.ApiMockerSettings;
import org.yml.plugin.constants.AnnotationsConstant;
import org.yml.plugin.enums.MockerTypeEnum;
import org.yml.plugin.enums.RequestMappingEnum;
import org.yml.plugin.enums.RequestMethodEnum;
import org.yml.plugin.handler.FieldItemResolver;
import org.yml.plugin.module.ApiMockerRequest;
import org.yml.plugin.module.MethodInfo;
import org.yml.plugin.module.swagger.SwaggerBean;
import org.yml.plugin.properties.ApiMockerProperties;
import org.yml.plugin.util.DescUtils;
import org.yml.plugin.util.HttpUtils;
import org.yml.plugin.util.NotificationUtils;
import org.yml.plugin.util.PsiAnnotationSearchUtil;
import org.yml.plugin.util.SpringMvcUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.yml.plugin.constants.Constants.ACTION_NAME_UPLOAD_TO_API_MOCKER;

/**
 * 自动提取接口信息上传到 api mocker
 *
 * @author yaml
 * @since 2020/12/28
 */
@Slf4j
public class UploadToApiMockerAction extends AbstractAction {

    /**
     * 缓存类  防止对象循环嵌套 导致栈溢出异常
     */
    public static Set<String> CLASS_CACHE = Sets.newHashSetWithExpectedSize(100);

    protected UploadToApiMockerAction() {
        super(ACTION_NAME_UPLOAD_TO_API_MOCKER, "resolve api and upload to api-mocker", null);
    }

    @Override
    protected void finallyInvoke() {
        super.finallyInvoke();
        clearCache();
    }

    public static void storeClass(String className) {
        CLASS_CACHE.add(className);
    }

    public static void clearCache() {
        CLASS_CACHE.clear();
    }


    public static boolean existClass(String className) {
        return CLASS_CACHE.contains(className);
    }

    @Override
    public void action(@NotNull AnActionEvent e) throws Exception {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        final PsiClass currentClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        Assert.assertNotNull("class not found", currentClass);
        final PsiMethod currentPsiMethod = PsiTreeUtil.getParentOfType(referenceAt, PsiMethod.class);
        String path = psiFile.getVirtualFile().getPath();
        final Module module = ModuleUtil.findModuleForFile(psiFile);
        final String classUri = SpringMvcUtils.resolveClassUri(currentClass);
        ApiMockerProperties apiMockerProperties = ApiMockerSettings.getInstance().getConfigByPath(project, path);
        Assert.assertNotNull("api-mocker properties not found", apiMockerProperties);
        Assert.assertTrue("配置缺失", apiMockerProperties.validate());
        PsiClass globalReturnClass = null;
        if (StringUtils.isNotBlank(apiMockerProperties.getReturnClass())) {
            globalReturnClass = JavaPsiFacade.getInstance(project).findClass(apiMockerProperties.getReturnClass(), GlobalSearchScope.allScope(project));
            Assert.assertNotNull("class not find: " + apiMockerProperties.getReturnClass(), globalReturnClass);
        }
        // 1、解析出鼠标所在位置的方法或类的api接口
        final PsiClass returnClass = globalReturnClass;
        List<MethodInfo> methodInfos = resolveClassMethodInfos(e, currentClass, currentPsiMethod, returnClass);
        methodInfos.forEach(methodInfo -> fillOtherInfo(methodInfo, classUri));
        // 2、转换成指定mocker服务的数据结构
        final SwaggerBean swaggerBean = SwaggerBean.convert(methodInfos);
        // 3、调用接口保存
        final ApiMockerRequest apiMockerRequest = ApiMockerRequest.instance(apiMockerProperties, swaggerBean);
        final JSONObject response = HttpUtils.postJson(apiMockerProperties.getUploadUrl(), apiMockerRequest);
        Optional.ofNullable(response.getJSONArray("data")).ifPresent(array -> array.forEach(url ->
                NotificationUtils.info(HotchpotchBundle.message("upload.to.api.mocker.success", url))));
    }

    private void fillOtherInfo(MethodInfo methodInfo, String classUri) {
        if (StringUtils.isNotBlank(classUri)) {
            final String uri = classUri.concat(methodInfo.getRequestUri()).replace("//", "/");
            methodInfo.setRequestUri(uri);
        }
    }

    private List<MethodInfo> resolveClassMethodInfos(AnActionEvent e, PsiClass currentClass, PsiMethod currentPsiMethod, PsiClass returnClass) {

        // 单个接口
        if (currentPsiMethod != null) {
            MethodInfo methodInfo = resolveMethodInfo(e, currentPsiMethod, returnClass);
            final LinkedList<MethodInfo> methods = Lists.newLinkedList();
            Optional.ofNullable(methodInfo).ifPresent(methods::add);
            return methods;
        }
        // 整个类
        PsiMethod[] psiMethods = currentClass.getMethods();
        return Arrays.stream(psiMethods)
                .map(psiMethod -> resolveMethodInfo(e, psiMethod, returnClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private MethodInfo resolveMethodInfo(AnActionEvent e, PsiMethod psiMethod, PsiClass returnClass) {
        // 解析方法上的请求头信息
        final PsiAnnotation[] annotations = psiMethod.getAnnotations();
        final PsiAnnotation psiAnnotation = Arrays.stream(annotations)
                .filter(annotation -> RequestMappingEnum.of(annotation.getQualifiedName()) != null)
                .findFirst().orElse(null);
        if (psiAnnotation == null) {
            return null;
        }
        final MethodInfo methodInfo = MethodInfo.create(psiMethod.getName());
        final RequestMappingEnum mappingEnum = RequestMappingEnum.of(psiAnnotation.getQualifiedName());
        final PsiNameValuePair[] attributes = psiAnnotation.getParameterList().getAttributes();
        // 解析请求方法类型
        RequestMethodEnum requestMethodEnum = SpringMvcUtils.resolveRequestMethodEnum(mappingEnum, attributes);
        methodInfo.setRequestMethodEnum(requestMethodEnum);
        // 解析请求路径
        String methodUri = SpringMvcUtils.resolveMethodUri(attributes);
        methodInfo.setRequestUri(methodUri);
        // 解析方法备注
        methodInfo.setCommentDoc(DescUtils.getFirstLineDesc(psiMethod.getDocComment()));
        methodInfo.setDescription(DescUtils.getDescription(psiMethod.getDocComment()));
        // 解析请求参数
        final MethodInfo.FieldItem bodyParams = resolveBodyParams(e, psiMethod);
        methodInfo.setBodyParams(bodyParams);
        clearCache();
        final LinkedList<MethodInfo.FieldItem> pathParams = resolvePathParams(e, psiMethod);
        methodInfo.setPathParams(pathParams);
        final LinkedList<MethodInfo.FieldItem> queryParams = resolveQueryParams(e, psiMethod);
        methodInfo.setQueryParams(queryParams);
        clearCache();
        // 解析返回结果
        MethodInfo.FieldItem response = FieldItemResolver.create(returnClass, Objects.requireNonNull(psiMethod.getReturnType()));
        methodInfo.setResponse(response);
        clearCache();
        return methodInfo;
    }

    private LinkedList<MethodInfo.FieldItem> resolveQueryParams(AnActionEvent e, PsiMethod psiMethod) {
        final PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        Map<String, String> mapParamDesc = DescUtils.mapParamDesc(psiMethod.getDocComment());
        return Arrays.stream(parameters)
                .filter(parameter ->
                        !PsiAnnotationSearchUtil.checkAnnotationsSimpleNameExistsIn(parameter, Arrays.asList(AnnotationsConstant.PathVariable, AnnotationsConstant.RequestBody))
                                && !"HttpServletRequest".equals(parameter.getType().getPresentableText())
                                && !"HttpServletResponse".equals(parameter.getType().getPresentableText()))
                .map(FieldItemResolver::create)
                .peek(fieldItem -> {
                    // 解析方法上面的 @param 参数备注
                    String desc = mapParamDesc.get(fieldItem.getName());
                    if (StringUtils.isBlank(fieldItem.getDesc())) {
                        fieldItem.setDesc(desc);
                    }
                })
                .flatMap(fieldItem -> {
                    if (fieldItem.getMockerType() == MockerTypeEnum.Array) {
                        // 如果是集合接收get参数 则取集合泛型数据类型
                        fieldItem.getChildren().stream().findFirst().ifPresent(child -> {
                            fieldItem.setMockerType(child.getMockerType());
                            fieldItem.setExample(child.getExample());
                        });
                        return Stream.of(fieldItem);
                    } else if (!fieldItem.getChildren().isEmpty()) {
                        // 如果是对象接收get参数 则取对象中的字段
                        return fieldItem.getChildren().stream();
                    }
                    return Stream.of(fieldItem);
                })
                .collect(Collectors.toCollection(Lists::newLinkedList));
    }

    private LinkedList<MethodInfo.FieldItem> resolvePathParams(AnActionEvent e, PsiMethod psiMethod) {
        final PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        Map<String, String> mapParamDesc = DescUtils.mapParamDesc(psiMethod.getDocComment());
        return Arrays.stream(parameters)
                .filter(parameter -> PsiAnnotationSearchUtil.checkAnnotationsSimpleNameExistsIn(parameter, Collections.singleton(AnnotationsConstant.PathVariable)))
                .map(FieldItemResolver::create)
                .peek(fieldItem -> {
                    // 解析方法上面的 @param 参数备注
                    String desc = mapParamDesc.get(fieldItem.getName());
                    if (StringUtils.isBlank(fieldItem.getDesc())) {
                        fieldItem.setDesc(desc);
                    }
                })
                .collect(Collectors.toCollection(Lists::newLinkedList));
    }

    private MethodInfo.FieldItem resolveBodyParams(AnActionEvent e, PsiMethod psiMethod) {
        final PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        return Arrays.stream(parameters)
                .filter(parameter -> PsiAnnotationSearchUtil.checkAnnotationsSimpleNameExistsIn(parameter, Collections.singleton(AnnotationsConstant.RequestBody)))
                .findFirst()
                .map(FieldItemResolver::create)
                .orElse(null);
    }
}
