package com.tanner.module.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Pair;
import com.tanner.base.BusinessException;
import com.tanner.base.ProjectManager;
import com.tanner.module.UapModuleBuilder;
import com.tanner.module.UapModuleType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModuleUtil {


    /**
     * 普通java模块
     **/
    public final static int MODULE_TYPE_JAVA = 0;
    /**
     * nc模块
     **/
    public final static int MODULE_TYPE_NC = 1;
    /**
     * maven模块
     **/
    public final static int MODULE_TYPE_MAVEN = 2;

    /**
     * 选中目录转换为module
     */
    public void coverToModule(Project project, String filePath) throws BusinessException {
        File file = new File(filePath);
        String modulePath = filePath;
        String moduleFileName = getModuleFileName(file);
        //0 是常规java模块，1是nc模块，2是maven模块
        int moduleType = MODULE_TYPE_JAVA;
        if (moduleFileName.startsWith("nc_")) {
            moduleFileName = moduleFileName.substring(3);
            moduleType = MODULE_TYPE_NC;
        } else if (moduleFileName.startsWith("maven_")) {
            moduleFileName = moduleFileName.substring(6);
            moduleType = MODULE_TYPE_MAVEN;
        }
        Library[] libraries = ProjectManager.getInstance().getProjectLibraries(project);
        Module module = ProjectManager.getInstance().getModule(file.getName());
        if (module == null) {
            //创建module
            UapModuleBuilder builder = new UapModuleType().createModuleBuilder();
            builder.setModuleFilePath(modulePath + File.separator + moduleFileName);
            builder.setContentEntryPath(modulePath);
            builder.setName(file.getName());
            List<Pair<String, String>> list = getSourcePathList(moduleType, modulePath);
            builder.setSourcePaths(list);
            builder.setLibraries(libraries);
            builder.commitModule(project, null);
        }
    }

    /**
     * 扫描source目录
     *
     * @param moduleType
     * @param modulePath
     * @return
     */
    private List<Pair<String, String>> getSourcePathList(int moduleType, String modulePath) {
        List<Pair<String, String>> list = new ArrayList<>();
        switch (moduleType) {
            case MODULE_TYPE_NC -> list = scanNCSourcePath(modulePath);
            case MODULE_TYPE_MAVEN -> list.add(new Pair<>(modulePath + "/src/main/java", ""));
            default -> list.add(new Pair<>(modulePath + "/src", ""));
        }
        return list;
    }

    private List<Pair<String, String>> scanNCSourcePath(String modulePath) {
        List<Pair<String, String>> list = new ArrayList();
        File moduleFile = new File(modulePath);
        for (File componentFile : moduleFile.listFiles()) {
            if (componentFile.isFile()) {
                continue;
            }
            File file = new File(componentFile.getPath() + File.separator + "component.xml");
            //如果模块下边组件文件存在
            if (file.exists()) {
                File srcFile = new File(file.getParent() + File.separator + "src");
                if (srcFile.exists()) {
                    for (File f : srcFile.listFiles()) {
                        if (f.getName().equals("client") || f.getName().equals("public") || f.getName()
                                .equals("private")) {
                            list.add(new Pair<>(f.getPath(), ""));
                        }
                    }
                }
            }
        }
        return list;
    }


    private String getModuleFileName(File file) {
        String moduleName = file.getName();
        String path = file.getPath();
        String ncModulePath = path + File.separator + "META-INF" + File.separator + "module.xml";
        String mavenModulePath = path + File.separator + "pom.xml";
        try {
            File mavenModuleFile = new File(mavenModulePath);
            File ncModuleFile = new File(ncModulePath);
            moduleName = "maven_" + file.getName();
            if (ncModuleFile.exists()) {
                moduleName = "nc_" + file.getName();
            } else {

            }
        } catch (Exception e) {
        }
        moduleName += ".iml";
        return moduleName;
    }
}
