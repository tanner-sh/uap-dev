package com.tanner.upm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.BaseUtil;
import com.tanner.base.ModuleRootUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EjbConfCopyUtil {

    /**
     * 递归路径获取可导出的文件
     *
     * @param filePath filePath
     */
    private Set<String> getFileUrl(String filePath, ProgressIndicator indicator)
            throws IOException {
        Path source = Path.of(filePath).toAbsolutePath().normalize();
        if (!Files.exists(source)) {
            return Set.of();
        }
        try (Stream<Path> paths = Files.isDirectory(source)
                ? Files.walk(source) : Stream.of(source)) {
            return paths.peek(path -> checkCanceled(indicator))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        Path parent = path.getParent();
                        return (name.endsWith(".rest") || name.endsWith(".upm"))
                                && parent != null
                                && "META-INF".equals(parent.getFileName().toString());
                    })
                    .map(Path::toString)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /**
     * 只拷贝选中的模块的upm文件到home
     *
     * @param event event
     */
    public int copy(AnActionEvent event) throws Exception {
        String homePath = UapProjectEnvironment.getInstance(event.getProject()).getUapHomePath();
        if (homePath.isBlank()) {
            throw new IllegalArgumentException("Not set NC Home");
        }
        Module module = BaseUtil.getModule(event);
        String ncModuleName = getNCModuleName(module);
        if (ncModuleName == null || ncModuleName.isBlank()) {
            throw new IllegalArgumentException("Can't determine NC module name");
        }
        VirtualFile selected = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selected == null) {
            throw new IllegalArgumentException("请选择 UPM/REST 文件或目录");
        }
        return copy(homePath, ncModuleName, selected.getPath(), null);
    }

    int copy(String homePath, String ncModuleName, String selectedPath,
             ProgressIndicator indicator) throws Exception {
        if (homePath == null || homePath.isBlank()) {
            throw new IllegalArgumentException("Not set NC Home");
        }
        if (ncModuleName == null || ncModuleName.isBlank()) {
            throw new IllegalArgumentException("Can't determine NC module name");
        }
        if (selectedPath == null || selectedPath.isBlank()) {
            throw new IllegalArgumentException("请选择 UPM/REST 文件或目录");
        }
        Path moduleSegment = Path.of(ncModuleName);
        if (moduleSegment.isAbsolute() || moduleSegment.getNameCount() != 1
                || ".".equals(ncModuleName) || "..".equals(ncModuleName)) {
            throw new IllegalArgumentException("非法 NC 模块名称: " + ncModuleName);
        }
        Path modulesRoot = Path.of(homePath, "modules").toAbsolutePath().normalize();
        Path targetRoot = modulesRoot.resolve(moduleSegment).resolve("META-INF").normalize();
        if (!targetRoot.startsWith(modulesRoot)) {
            throw new IllegalArgumentException("非法 NC 模块名称: " + ncModuleName);
        }
        Files.createDirectories(targetRoot);
        Set<String> fileUrls = getFileUrl(selectedPath, indicator);
        List<String> errorList = new ArrayList<>();
        int copied = 0;
        for (String fileUrl : fileUrls) {
            checkCanceled(indicator);
            File file = new File(fileUrl);
            try {
                // 逐个拷贝到home
                Files.copy(file.toPath(), targetRoot.resolve(file.getName()),
                        StandardCopyOption.REPLACE_EXISTING);
                copied++;
            } catch (IOException ignored) {
                errorList.add(file.getName());
            }
        }
        if (!errorList.isEmpty()) {
            throw new IOException("文件" + errorList + "拷贝出错");
        }
        return copied;
    }

    private void checkCanceled(ProgressIndicator indicator) {
        if (indicator != null) {
            indicator.checkCanceled();
        }
    }

    /**
     * 获取nc模块名称
     *
     * @param module
     * @return
     */
    String getNCModuleName(Module module) {
        String ncModuleName = null;
        VirtualFile moduleRoot = ModuleRootUtil.findPrimaryContentRoot(module);
        if (moduleRoot == null) {
            return null;
        }
        String modulePath = moduleRoot.getPath();
        try {
            File file = new File(
                    modulePath + File.separator + "META-INF" + File.separator + "module.xml");
            if (file.exists()) {
                Document doc;
                try (InputStream in = new FileInputStream(file)) {
                    doc = XmlUtil.parse(in);
                }
                Element root = doc.getDocumentElement();
                ncModuleName = root.getAttribute("name");
            }
        } catch (Exception e) {
            //抛错就认为不是nc项目
        }
        return ncModuleName;
    }
}
