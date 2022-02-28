package org.yml.plugin;

import com.intellij.CommonBundle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * @author yaml
 * @since 2021/3/2
 */
@SuppressWarnings("ALL")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HotchpotchBundle {

    /**
     * The {@link ResourceBundle} path.
     */
    @NonNls
    private static final String BUNDLE_NAME = "messages.HotchpotchBundle";

    /**
     * The {@link ResourceBundle} instance.
     */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static @Nls String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        // 兼容老版本 新版本使用：return AbstractBundle.message(BUNDLE, key, params);
        return CommonBundle.message(BUNDLE, key, params);
    }
}
