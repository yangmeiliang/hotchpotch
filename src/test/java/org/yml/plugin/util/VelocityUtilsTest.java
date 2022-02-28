package org.yml.plugin.util;

import org.junit.Test;

public class VelocityUtilsTest {

    @Test
    public void test01() {
        final String s = VelocityUtils.loadText("/template/entity.java.vm");
        System.out.println(s);
    }
}