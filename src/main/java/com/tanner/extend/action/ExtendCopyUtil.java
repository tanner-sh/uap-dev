package com.tanner.extend.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.BusinessException;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

public final class ExtendCopyUtil {

    public static final String HOME_CONFIG_FILE_PATH =
            File.separator + "hotwebs" + File.separator + "nccloud" + File.separator + "WEB-INF"
                    + File.separator + "extend" + File.separator + "yyconfig";

    private ExtendCopyUtil() {
    }

    public static int copyToNCHome(AnActionEvent event) throws Exception {
        String homePath = UapProjectEnvironment.getInstance(event.getProject()).getUapHomePath();
        if (StringUtils.isBlank(homePath)) {
            throw new BusinessException("Not set NC Home");
        }
        VirtualFile selected = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selected == null) {
            throw new BusinessException("请选择鉴权文件或目录");
        }
        Path source = Path.of(selected.getPath()).toAbsolutePath().normalize();
        Path targetRoot = Path.of(homePath + HOME_CONFIG_FILE_PATH)
                .toAbsolutePath().normalize();
        List<Path> files;
        if (Files.isDirectory(source)) {
            try (Stream<Path> stream = Files.walk(source)) {
                files = stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".xml"))
                        .toList();
            }
        } else {
            files = List.of(source);
        }
        int copied = 0;
        for (Path file : files) {
            if (!file.getFileName().toString().endsWith(".xml")) {
                continue;
            }
            Path relative = relativeAfterSegment(file, "yyconfig");
            if (relative == null) {
                continue;
            }
            Path target = targetRoot.resolve(relative).normalize();
            if (!target.startsWith(targetRoot)) {
                throw new BusinessException("非法鉴权文件路径: " + file);
            }
            Files.createDirectories(target.getParent());
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            copied++;
        }
        return copied;
    }

    private static Path relativeAfterSegment(Path path, String segment) {
        for (int i = path.getNameCount() - 2; i >= 0; i--) {
            if (segment.equals(path.getName(i).toString()) && i + 1 < path.getNameCount()) {
                return path.subpath(i + 1, path.getNameCount());
            }
        }
        return null;
    }
}
