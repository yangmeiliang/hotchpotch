package org.yml.plugin.module;

import org.yml.plugin.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yaml
 * @since 2021/1/6
 */
public class KV<K, V> extends LinkedHashMap<K, V> {

    public static <K, V> KV<K, V> create() {
        return new KV<K, V>();
    }

    public KV<K, V> put(Map<K, V> map) {
        super.putAll(map);
        return this;
    }

    public KV<K, V> put(KV<K, V> kv) {
        super.putAll(kv);
        return this;
    }

    public KV<K, V> delete(K key) {
        super.remove(key);
        return this;
    }

    public String toPrettyJson() {
        return JsonUtil.toPrettyJson(this);
    }
}
