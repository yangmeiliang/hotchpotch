package org.yml.plugin.enums;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
@SuppressWarnings("ALL")
public enum CollectionTypesEnum {

    LIST("List", Lists.newArrayList(), MockerTypeEnum.Array),
    COLLECTION("Collection", Lists.newArrayList(), MockerTypeEnum.Array),
    ARRAY_LIST("ArrayList", Lists.newArrayList(), MockerTypeEnum.Array),
    LINKED_LIST("LinkedList", Lists.newLinkedList(), MockerTypeEnum.Array),

    SET("Set", Sets.newHashSet(), MockerTypeEnum.Array),
    LINKED_HASH_SET("LinkedHashSet", Sets.newLinkedHashSet(), MockerTypeEnum.Array),

    MAP("Map", Maps.newHashMap(), MockerTypeEnum.Object),
    HASH_MAP("HashMap", Maps.newHashMap(), MockerTypeEnum.Object),
    LINKED_HASH_MAP("LinkedHashMap", Maps.newLinkedHashMap(), MockerTypeEnum.Object),

    ARRAY_INTEGER("Integer[]", Lists.newArrayList(), MockerTypeEnum.Array),
    ARRAY_STRING("String[]", Lists.newArrayList(), MockerTypeEnum.Array),

    ;

    private final String shortName;
    private final Object defaultValue;
    private final MockerTypeEnum mockerType;

    public boolean eq(String shortName) {
        return Objects.equals(shortName, this.shortName);
    }

    public static Optional<CollectionTypesEnum> of(String typeName) {
        final String shortName = PsiTypeUtils.extractShortName(typeName);
        return Arrays.stream(CollectionTypesEnum.values()).filter(o -> o.eq(shortName)).findFirst();
    }
}
