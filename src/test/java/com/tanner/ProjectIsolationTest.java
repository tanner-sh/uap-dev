package com.tanner;

import com.intellij.openapi.project.Project;
import com.tanner.base.ProjectManager;
import com.tanner.base.UapProjectEnvironment;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ProjectIsolationTest {

    @Test
    public void projectManagerDoesNotRetainGlobalProject() {
        for (Field field : ProjectManager.class.getDeclaredFields()) {
            boolean staticProject = Modifier.isStatic(field.getModifiers())
                    && Project.class.isAssignableFrom(field.getType());
            assertFalse("ProjectManager must not retain a static Project", staticProject);
        }
    }

    @Test
    public void environmentInstancesKeepIndependentState() {
        UapProjectEnvironment first = new UapProjectEnvironment();
        UapProjectEnvironment second = new UapProjectEnvironment();
        first.setUapHomePath("/first");
        second.setUapHomePath("/second");

        assertEquals("/first", first.getUapHomePath());
        assertEquals("/second", second.getUapHomePath());
    }
}
