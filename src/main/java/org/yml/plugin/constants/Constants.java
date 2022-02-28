package org.yml.plugin.constants;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;

/**
 * @author yaml
 * @since 2021/7/5
 */
public interface Constants {

    String ACTION_ID_TRIAL_RESET = "org.yml.plugin.actions.TrialResetAction";
    String ACTION_ID_RESTART = "org.yml.plugin.actions.RestartAction";

    String ACTION_NAME_GENERATE_O2O = "HP GenerateO2O";
    String ACTION_NAME_CONVERT_TO_JSON = "HP ConvertToJson";
    String ACTION_NAME_UPLOAD_TO_API_MOCKER = "HP UploadToApiMocker";
    String ACTION_NAME_CODE_GENERATE = "HP CodeGenerateFromCreateSql";
    String ACTION_NAME_TRIAL_RESET = "Trial Reset";
    String ACTION_NAME_RESTART = "IDE Restart";

    String PLUGIN_ID_STR = "org.yml.plugin.hotchpotch";
    String NOTIFICATION_DISPLAY_ID = "Hotchpotch.NotificationGroup";

    String PRODUCT_NAME = ApplicationNamesInfo.getInstance().getProductName();
    String PRODUCT_NAME_LOWER = PRODUCT_NAME.toLowerCase();
    String HOME_PATH_HEX = Integer.toHexString(FileUtil.pathHashCode(PathManager.getHomePath()));
    String PLUGIN_PREFS_PREFIX = PLUGIN_ID_STR;


}
