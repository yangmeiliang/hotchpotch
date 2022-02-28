package org.yml.plugin.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;

/**
 * @author yaml
 * @since 2021/1/6
 */
@Getter
@Setter
@AllArgsConstructor
public class ProjectClassLoader extends ClassLoader {

    private final String classPath;


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        final byte[] data = loadByte(name);
        return defineClass(name, data, 0, data.length);
    }

    private byte[] loadByte(@NotNull String name) throws ClassNotFoundException {
        try {
            final String filePath = classPath + "/" + name.replaceAll("\\.", "/") + ".class";
            FileInputStream fis = new FileInputStream(filePath);
            int len = fis.available();
            byte[] data = new byte[len];
            fis.read(data);
            fis.close();
            return data;
        } catch (Exception e) {
            throw new ClassNotFoundException();
        }
    }
}
