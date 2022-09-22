package com.tanner.base;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassLoaderUtil {

    public static ClassLoader getUapJdbcClassLoader(String homePath) throws BusinessException {
        try {
            //加载home/lib以及driver下所有jar包
            Collection<File> libJars = FileUtils.listFiles(new File(homePath, "lib"), null, true);
            Collection<File> driverJars = FileUtils.listFiles(new File(homePath, "driver"), null, true);
            Collection<File> allJars = CollectionUtils.union(libJars, driverJars);
            Map<String, File> libJarsMap = allJars.stream().collect(
                    Collectors.toMap(File::getName, Function.identity(), (oldValue, newValue) -> newValue));
            URL[] urls = FileUtils.toURLs(libJarsMap.values().toArray(new File[0]));
            return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        } catch (IOException e) {
            throw new BusinessException("读取驱动失败!" + e.getMessage());
        }
    }

}
