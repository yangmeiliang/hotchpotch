package org.yml.plugin.enums;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yml.plugin.util.PsiTypeUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yaml
 * @since 2020/12/31
 */
@Getter
@AllArgsConstructor
public enum ListTypeEnum {

    /**
     * 有序集合
     */
    LIST("List", Lists.newArrayList(), MockerTypeEnum.Array),
    ARRAY_LIST("ArrayList", Lists.newArrayList(), MockerTypeEnum.Array),
    LINKED_LIST("LinkedList", Lists.newLinkedList(), MockerTypeEnum.Array),

    /**
     * 无序集合
     */
    SET("Set", Sets.newHashSet(), MockerTypeEnum.Array),
    LINKED_HASH_SET("LinkedHashSet", Sets.newLinkedHashSet(), MockerTypeEnum.Array),
    ;

    private final String shortName;
    private final Object defaultValue;
    private final MockerTypeEnum mockerType;

    public boolean eq(String shortName) {
        return Objects.equals(shortName, this.shortName);
    }

    public static Optional<ListTypeEnum> of(String typeName) {
        final String shortName = PsiTypeUtils.extractShortName(typeName);
        return Arrays.stream(ListTypeEnum.values()).filter(o -> o.eq(shortName)).findFirst();
    }
}
