package com.tanner.library.action;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.tanner.base.BusinessException;
import com.tanner.base.ClassPathConstantUtil;
import com.tanner.base.ProjectManager;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LibrariesUtil {

    public static void setLibraries(String homePath) throws BusinessException {
        //nc类路径
        List<String> ncLibraries = ClassPathConstantUtil.getNCLibrary();
        //当前工程
        Project project = ProjectManager.getInstance().getProject();
        //判断nchome是否存在
        if (StringUtils.isBlank(homePath)) {
            throw new BusinessException("请先设置home路径");
        }
        File homeFile = new File(homePath);
        if (!homeFile.exists()) {
            throw new BusinessException("home不存在，请检查");
        }
        //首先创建库
        LibraryTable.ModifiableModel model = LibraryTablesRegistrar.getInstance()
                .getLibraryTable(project).getModifiableModel();
        Map<String, Library> libraryMap = new HashMap<>();
        for (String libraryName : ncLibraries) {
            //根据库名获取库
            LibraryEx library = (LibraryEx) model.getLibraryByName(libraryName);
            // 库不存在创建新的
            if (library == null) {
                library = (LibraryEx) model.createLibrary(libraryName);
            }
            libraryMap.put(libraryName, library);
        }
        /*扫描 nc home */
        //设置ant
        String antPath = homePath + File.separator + "ant";
        List<String> antUrl = scanJarAndClasses(antPath, true, false);
        //设置framework
        String frameworkPath = homePath + File.separator + "framework";
        List<String> frameworkList = scanJarAndClasses(frameworkPath, false, false);
        //设置middleware
        String middlewarePath = homePath + File.separator + "middleware";
        List<String> middlewareList = scanJarAndClasses(middlewarePath, false, true);
        //扫描lang目录
        String langPath = homePath + File.separator + "langlib";
        List<String> langList = scanJarAndClasses(langPath, false, false);
        //扫描hotwebs
        String externalPath = homePath + File.separator + "external";
        hotwebEspecial(homePath, externalPath, "nccloud", "ncchr", "fbip");//移动pub_platform到external
        //扫描lib 和 external
        String libPath = homePath + File.separator + "lib";
        List<String> libList = scanJarAndClasses(libPath, false, false);
        List<String> externalList = scanJarAndClasses(externalPath, true, true);
        List<String> productList = new ArrayList<>();
        productList.addAll(libList);
        productList.addAll(externalList);
        //扫描ejb目录
        String ejbPath = homePath + "ejb";
        List<String> ejbList = scanJarAndClasses(ejbPath, false, false);
        //扫描resource
        String resourcePath = homePath + File.separator + "resources";
        List<String> resourcesList = new ArrayList<>();
        resourcesList.add(resourcePath);
        //扫描modules
        String modulesPath = homePath + File.separator + "modules";
        Map<String, List<String>> moduleMap = scanModules(modulesPath);
        if (!moduleMap.isEmpty()) {
            for (String key : moduleMap.keySet()) {
                setLibrary(moduleMap.get(key), project, libraryMap.get(key).getModifiableModel());
            }
        }
        //设置类路径
        setLibrary(antUrl, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_ANT).getModifiableModel());
        setLibrary(frameworkList, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_FRAMEWORK).getModifiableModel());
        setLibrary(middlewareList, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_MIDDLEWARE).getModifiableModel());
        setLibrary(langList, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_LANG).getModifiableModel());
        setLibrary(productList, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_PRODUCT).getModifiableModel());
        setLibrary(ejbList, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_EJB).getModifiableModel());
        //        setLibrary(nccloudList, project, libraryMap.get(ClassPathConstantUtil.PATH_NAME_NCCLOUD).getModifiableModel());
        setLibrary(resourcesList, project,
                libraryMap.get(ClassPathConstantUtil.PATH_NAME_RESOURCES).getModifiableModel());
        WriteCommandAction.runWriteCommandAction(project, model::commit);
        ProjectManager.getInstance().setAllModuleLibrary();
    }

    /**
     * 设置依赖库
     *
     * @param urlSet       urlSet
     * @param project      project
     * @param libraryModel libraryModel
     */
    private static void setLibrary(List<String> urlSet, Project project,
                                   Library.ModifiableModel libraryModel) {
        //删除旧的库依赖
        String[] classUrl = libraryModel.getUrls(OrderRootType.CLASSES);
        String[] sourceUrl = libraryModel.getUrls(OrderRootType.SOURCES);
        for (String url : classUrl) {
            libraryModel.removeRoot(url, OrderRootType.CLASSES);
        }
        for (String url : sourceUrl) {
            libraryModel.removeRoot(url, OrderRootType.SOURCES);
        }
        //添加库依赖
        for (String url : urlSet) {
            File file = new File(url);
            if (file.exists()) {
                if (!file.getName().endsWith("classes") && !file.getName()
                        .endsWith("resources")) {//非补丁目录,非resources目录
                    libraryModel.addJarDirectory(VirtualFileManager.constructUrl("file", url), false);
                    libraryModel.addJarDirectory(VirtualFileManager.constructUrl("file", url), false,
                            OrderRootType.SOURCES);
                } else {
                    libraryModel.addRoot(VirtualFileManager.constructUrl("file", url), OrderRootType.CLASSES);
                    libraryModel.addRoot(VirtualFileManager.constructUrl("file", url), OrderRootType.SOURCES);
                }
            }
        }
        // 提交库变更
        WriteCommandAction.runWriteCommandAction(project, libraryModel::commit);
    }


    /**
     * 扫描指定目录下的lib 和classes
     *
     * @param basePath basePath
     * @param libFlag  libFlag
     * @return List<String>
     */
    private static List<String> scanJarAndClasses(String basePath, boolean libFlag,
                                                  boolean classFlag) {
        List<String> pathList = new ArrayList<>();
        basePath += File.separator;
        if (classFlag) {
            //扫描classes
            String classesPath = basePath + "classes";
            File classesFile = new File(classesPath);
            if (classesFile.exists()) {
                pathList.add(classesPath);
            }
        }
        //扫描lib
        String jarPath = basePath;
        if (libFlag) {
            jarPath += "lib";
        }
        File jarFile = new File(jarPath);
        if (jarFile.exists()) {
            pathList.add(jarPath);
        }
        //这里暂时不清楚有多少个extra。目前仅仅知道在basepp/MATA-INF下有，因此暂时默认所有的private代码可能会有extra
        String extraPath = basePath;
        if (basePath.contains("META-INF")) {
            extraPath += "extra";
            File extraFile = new File(basePath);
            if (extraFile.exists()) {
                pathList.add(extraPath);
            }
        }
        return pathList;
    }

    /**
     * hotweb下非ui类jar包转移到external下
     *
     * @param homePath     homePath
     * @param externalPath externalPath
     * @param webServers   webServers
     */
    private static void hotwebEspecial(String homePath, String externalPath, String... webServers) {
        String hotwebsPath = homePath + File.separator + "hotwebs";
        for (String server : webServers) {
            File hotwebFile = new File(
                    hotwebsPath + File.separator + server + File.separator + "WEB-INF" + File.separator
                            + "lib");
            File externalFile = new File(externalPath + File.separator + "lib");
            if (!hotwebFile.exists() || !externalFile.exists()) {
                continue;
            }
            File[] files = hotwebFile.listFiles();
            if (files == null) {
                continue;
            }
            boolean isNCCloudFlag = server.equals("nccloud") || server.equals("fbip");
            StringBuilder jarBuffer = new StringBuilder();
            for (File file : files) {
                try {
                    //nccloud的jar需要解压提取鉴权文件
                    if (file.getName().endsWith("jar") && !file.getName().contains("_src")) {
                        jarBuffer.append(",").append(file.getName());
                        if (isNCCloudFlag) {
                            unZip(homePath, server, file);
                        }
                    }
                    File newFile = new File(externalFile.getPath() + File.separator + file.getName());
                    if (newFile.exists()) {
                        newFile.delete();
                    }
                    //jar包复制到external/lib下
                    FileUtil.copy(file, newFile);
                    //复制后删除
                    file.delete();
                } catch (IOException ignored) {

                }
            }
            //复制classes文件夹到external目录下
            try {
                File fromPath = new File(hotwebsPath + File.separator + server + File.separator + "WEB-INF" + File.separator + "classes");
                if (fromPath.exists()) {
                    File toPath = new File(externalPath + File.separator + "classes");
                    FileUtil.copyFileOrDir(fromPath, toPath);
                    for (File file : Objects.requireNonNull(fromPath.listFiles())) {
                        FileUtil.delete(file);
                    }
                }
            } catch (IOException ignored) {

            }
            String str = jarBuffer.toString();
            if (str.startsWith(",")) {
                str = str.substring(1);
                switch (server) {
                    case "nccloud":
                        UapProjectEnvironment.getInstance().setNccloudJar(str);
                        break;
                    case "ncchr":
                        UapProjectEnvironment.getInstance().setNcchrJAR(str);
                        break;
                }
            }
        }
    }

    /**
     * 读取jar包
     *
     * @param jarFile jarFile
     */
    private static void unZip(String homePath, String server, File jarFile) throws IOException {
        String outPath =
                homePath + File.separator + "hotwebs" + File.separator + server + File.separator
                        + "WEB-INF" + File.separator + "extend";
        JarFile jar = new JarFile(jarFile.getPath());
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            if (isConfigResource(name)) {
                System.out.println(name);
                InputStream inputStream = jar.getInputStream(entry);
                BufferedInputStream in = new BufferedInputStream(inputStream);
                File file = new File(outPath + File.separator + name);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file.getPath()));
                int len = -1;
                byte[] b = new byte[1024];
                while ((len = in.read(b)) != -1) {
                    out.write(b, 0, len);
                }
                in.close();
                out.close();
            }
        }
        jar.close();
    }

    /**
     * 扫描nc module
     *
     * @param modulesPath modulesPath
     * @return Map<String, List < String>>
     */
    private static Map<String, List<String>> scanModules(String modulesPath) {
        Map<String, List<String>> jarMap = new HashMap<>();
        File modulesFile = new File(modulesPath);
        File[] modules = modulesFile.listFiles();
        if (modules == null) {
            return jarMap;
        }
        List<String> publicLibrarySet = new ArrayList<>();
        List<String> privateLibrarySet = new ArrayList<>();
        List<String> clientLibrarySet = new ArrayList<>();
        for (File module : modules) {
            String modulePath = module.getPath();
            String clientPath = modulePath + File.separator + "client";
            String privatePath = modulePath + File.separator + "META-INF";
            publicLibrarySet.addAll(scanJarAndClasses(modulePath, true, true));
            privateLibrarySet.addAll(scanJarAndClasses(privatePath, true, true));
            clientLibrarySet.addAll(scanJarAndClasses(clientPath, true, true));
        }
        jarMap.put(ClassPathConstantUtil.PATH_NAME_PUBLIC, publicLibrarySet);
        jarMap.put(ClassPathConstantUtil.PATH_NAME_PRIVATE, privateLibrarySet);
        jarMap.put(ClassPathConstantUtil.PATH_NAME_CLIENT, clientLibrarySet);
        return jarMap;
    }


    private static boolean isConfigResource(String name) {
        boolean flag = false;
        if (!name.startsWith("yyconfig")) {
            return false;
        }
        flag = name.contains(".xml");
        if (!flag) {
            flag = name.contains(".json");
        }
        if (flag) {
            flag = !isSystemConfig(name);
        }
        return flag;
    }

    private static boolean isSystemConfig(String name) {
        String[] systemNames = new String[]{"yyconfig/configreader/configreader.xml", "log.xml",
                "yyconfig/baseapi/baseapi.xml"};
        for (String systemName : systemNames) {
            if (name.contains(systemName)) {
                return true;
            }
        }
        return false;
    }
}
