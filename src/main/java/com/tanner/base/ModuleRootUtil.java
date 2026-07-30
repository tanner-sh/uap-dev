package com.tanner.base;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves module content roots through the public IntelliJ workspace-model API.
 */
public final class ModuleRootUtil {

    private ModuleRootUtil() {
    }

    public static @Nullable VirtualFile findPrimaryContentRoot(Module module) {
        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        if (roots.length == 0) {
            return null;
        }
        for (VirtualFile root : roots) {
            if (root.findFileByRelativePath("META-INF/module.xml") != null) {
                return root;
            }
        }
        return roots[0];
    }
}
