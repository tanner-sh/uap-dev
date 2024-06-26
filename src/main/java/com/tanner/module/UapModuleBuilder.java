package com.tanner.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.BusinessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ncc 模块创建器 用于将含有src的目录转为依赖nc的可调试java module
 */
public class UapModuleBuilder extends ModuleBuilder {

    private List<Pair<String, String>> mySourcePaths;
    private Library[] libraries;
    private String myCompilerOutputPath;

    @Override
    public ModuleType<?> getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Override
    public @Nullable List<Module> commit(@NotNull Project project, ModifiableModuleModel model,
                                         ModulesProvider modulesProvider) {
        LanguageLevelProjectExtension extension = LanguageLevelProjectExtension.getInstance(
                ProjectManager.getInstance().getDefaultProject());
        Boolean aDefault = extension.getDefault();
        LanguageLevelProjectExtension instance = LanguageLevelProjectExtension.getInstance(project);
        if (aDefault != null && !aDefault) {
            instance.setLanguageLevel(extension.getLanguageLevel());
        } else {
            Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (sdk != null) {
                JavaSdkVersion version = JavaSdk.getInstance().getVersion(sdk);
                if (version != null) {
                    instance.setLanguageLevel(version.getMaxLanguageLevel());
                    instance.setDefault(true);
                }
            }
        }
        return super.commit(project, model, modulesProvider);
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) {
        //设置jdk
        CompilerModuleExtension compilerModuleExtension = rootModel.getModuleExtension(
                CompilerModuleExtension.class);
        compilerModuleExtension.setExcludeOutput(true);
        if (this.myJdk != null) {
            rootModel.setSdk(this.myJdk);
        } else {
            rootModel.inheritSdk();
        }
        //设置source目录
        ContentEntry contentEntry = this.doAddContentEntry(rootModel);
        if (contentEntry != null) {
            List<Pair<String, String>> sourcePaths = this.getSourcePaths();
            if (sourcePaths != null) {
                for (Pair<String, String> path : sourcePaths) {
                    String moduleLibraryPath = path.getFirst();
                    VirtualFile sourceRoot = LocalFileSystem.getInstance()
                            .refreshAndFindFileByPath(FileUtil.toSystemIndependentName(moduleLibraryPath));
                    if (sourceRoot != null) {
                        contentEntry.addSourceFolder(sourceRoot, false, path.getSecond());
                    }
                }
            }
        }
        //设置输出目录
        if (this.myCompilerOutputPath != null) {
            String canonicalPath;
            try {
                canonicalPath = FileUtil.resolveShortWindowsName(this.myCompilerOutputPath);
            } catch (IOException var11) {
                canonicalPath = this.myCompilerOutputPath;
            }
            compilerModuleExtension.setCompilerOutputPath(VfsUtilCore.pathToUrl(canonicalPath));
        } else {
            compilerModuleExtension.inheritCompilerOutputPath(true);
        }
        //设置依赖
        if (this.libraries != null) {
            for (Library library : libraries) {
                rootModel.addLibraryEntry(library);
            }
        }
    }

    public List<Pair<String, String>> getSourcePaths() {
        if (this.mySourcePaths == null) {
            List<Pair<String, String>> paths = new ArrayList<>();
            String entryPath = this.getContentEntryPath();
            String path = entryPath + File.separator + "src";
            (new File(path)).mkdirs();
            paths.add(Pair.create(path, ""));
            return paths;
        } else {
            return this.mySourcePaths;
        }
    }

    public void setSourcePaths(List<Pair<String, String>> sourcePaths) {
        this.mySourcePaths = sourcePaths != null ? new ArrayList<>(sourcePaths) : null;
    }

    public void addSourcePath(Pair<String, String> sourcePathInfo) {
        this.mySourcePaths.add(sourcePathInfo);
    }

    public void setLibraries(Library[] libraries) {
        if (libraries == null) {
            try {
                libraries = com.tanner.base.ProjectManager.getInstance()
                        .getProjectLibraries(com.tanner.base.ProjectManager.getInstance().getProject());
            } catch (BusinessException ignored) {
            }
        }
        this.libraries = libraries;
    }

    public final void setCompilerOutputPath(String compilerOutputPath) {
        this.myCompilerOutputPath = acceptParameter(compilerOutputPath);
    }

}
