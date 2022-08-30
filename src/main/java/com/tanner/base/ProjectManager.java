package com.tanner.base;

import com.intellij.openapi.components.ServiceManager;
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
  private static ProjectManager instance;
  private static Project project;

  public static ProjectManager getInstance() {
    if (instance == null) {
      instance = new ProjectManager();
    }
    return instance;
  }

  public static ProjectManager getInstance(Project project) {
    if (instance == null) {
      instance = new ProjectManager();
    }
    if (instance.getProject() == null) {
      instance.setProject(project);
    }
    return instance;
  }

  /**
   * 获取当前project
   *
   * @return
   */
  public Project getProject() {
    if (project == null) {

    }
    return project;
  }

  public void setProject(Project project) {
    ProjectManager.project = project;
  }

  /**
   * 获取当前project下所有module
   *
   * @return
   */
  public Module[] getAllModule(Project project) {
    if (project == null) {
      project = getProject();
    }
    return ModuleManager.getInstance(project).getModules();
  }

  /**
   * 根据moduleName获得module
   *
   * @param moduleName
   * @return
   */
  public Module getModule(String moduleName) {
    Project project = getProject();
    return ModuleManager.getInstance(project).findModuleByName(moduleName);
  }

  /**
   * 获取工程library
   *
   * @param project
   * @return
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
   * @param project
   * @return
   */
  private LibraryTable getLibraryTable(Project project) {
    if (project == null) {
      project = getProject();
    }
    LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
    return libraryTable;
  }

  /**
   * 设置module的library
   *
   * @param module
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

  public void setAllModuleLibrary() throws BusinessException {
    Project project = getProject();
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
      t = ServiceManager.getService(project, clazz);
    } catch (Throwable e) {
      return null;
    }
    if (t == null) {
      LOG.error("Could not find service: " + clazz.getName());
      return null;
    }
    return t;
  }

  public <T> T getService(Class<T> clazz) {
    return getService(getProject(), clazz);
  }
}
