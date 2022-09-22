package com.tanner.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.ModuleType;

public class StdModuleTypes {

    public static final ModuleType<JavaModuleBuilder> JAVA;

    static {
        try {
            JAVA = (ModuleType) Class.forName("com.tanner.module.UapModuleType").newInstance();
        } catch (Exception var1) {
            throw new IllegalArgumentException(var1);
        }
    }

    public StdModuleTypes() {
    }
}
