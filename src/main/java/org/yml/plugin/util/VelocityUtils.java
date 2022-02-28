package org.yml.plugin.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.URLUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author yaml
 * @since 2021/2/23
 */
public interface VelocityUtils {

    Pattern PATTER_PARSE = Pattern.compile("(?<=^#parse\\(\\\").*(?=\\\"\\))", Pattern.CASE_INSENSITIVE);

    static String parseContent(String templateName, Map<String, String> contentMap) {
        return String.join("\n", parseText(templateName, contentMap));
    }

    static List<String> parseText(String templateName, Map<String, String> contentMap) {
        String[] lines = contentMap.get(templateName).split("\n");
        List<String> finalText = new ArrayList<>();
        for (String line : lines) {
            final String parseTemplate = PatternUtils.groupFirst(PATTER_PARSE, line);
            if (StringUtils.isNotBlank(parseTemplate)) {
                List<String> data = parseText(parseTemplate.replace(".vm", ""), contentMap);
                finalText.addAll(data);
            } else {
                finalText.add(line);
            }
        }
        return finalText;
    }

    static String loadText(String template) {
        try {
            URL resource = Thread.currentThread().getClass().getResource(template);
            if (resource == null) {
                resource = VelocityUtils.class.getResource(template);
            }
            BufferedReader stream = new BufferedReader(new InputStreamReader(URLUtil.openStream(resource)));
            return String.join("\n", FileUtil.loadLines(stream));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
