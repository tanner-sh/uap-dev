package com.tanner.base;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;

import java.util.ArrayList;
import java.util.List;


public class ProjectManager {

    private static final Logger LOG = Logger.getInstance(ProjectManager.class);
    private static final ProjectManager INSTANCE = new ProjectManager();

    public static ProjectManager getInstance() {
        return INSTANCE;
    }

    public static ProjectManager getInstance(Project project) {
        return INSTANCE;
    }

    /**
     * 获取当前project下所有module
     *
     * @return Module[]
     */
    public Module[] getAllModule(Project project) {
        return ModuleManager.getInstance(project).getModules();
    }

    /**
     * 根据moduleName获得module
     *
     * @param moduleName moduleName
     * @return Module
     */
    public Module getModule(Project project, String moduleName) {
        return ModuleManager.getInstance(project).findModuleByName(moduleName);
    }

    /**
     * 获取工程library
     *
     * @param project project
     * @return Library[]
     */
    public Library[] getProjectLibraries(Project project) throws BusinessException {
        List<String> list = ClassPathConstantUtil.getNCLibrary();
        List<Library> libraries = new ArrayList<>();
        LibraryTable libraryTable = getLibraryTable(project);
        for (String libName : list) {
            Library library = libraryTable.getLibraryByName(libName);
            if (library == null) {
                throw new BusinessException(300, "uap libraries缺失！请先到集成配置设置nc类路径\n");
            }
            libraries.add(library);
        }
        return libraries.toArray(new Library[0]);
    }

    /**
     * 获取LibraryTable
     *
     * @param project project
     * @return LibraryTable
     */
    private LibraryTable getLibraryTable(Project project) {
        return LibraryTablesRegistrar.getInstance().getLibraryTable(project);
    }

    /**
     * 设置module的library
     *
     * @param module module
     */
    public void setModuleLibrary(Project project, Module module) throws BusinessException {
        Library[] libraries = getProjectLibraries(project);
        if (libraries == null || libraries.length == 0) {
            throw new BusinessException("this project is not set uap libraries!");
        }
        for (Library library : libraries) {
            if (ModuleRootManager.getInstance(module).getModifiableModel().findLibraryOrderEntry(library)
                    == null) {
                ModuleRootModificationUtil.addDependency(module, library);
            }
        }
    }

    public void setAllModuleLibrary(Project project) throws BusinessException {
        Module[] modules = getAllModule(project);
        Library[] libraries = getProjectLibraries(project);
        for (Module module : modules) {
            if (module.getModuleFile() == null) {
                continue;
            }
            for (Library library : libraries) {
                if (ModuleRootManager.getInstance(module).getModifiableModel()
                        .findLibraryOrderEntry(library) == null) {
                    ModuleRootModificationUtil.addDependency(module, library);
                }
            }
        }
    }

    public <T> T getService(Project project, Class<T> clazz) {
        T t = null;
        try {
            t = project.getService(clazz);
        } catch (Throwable e) {
            return null;
        }
        if (t == null) {
            LOG.error("Could not find service: " + clazz.getName());
            return null;
        }
        return t;
    }

}
