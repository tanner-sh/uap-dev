package com.tanner.abs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractDialog extends DialogWrapper {

    private final Map<String, JComponent> componentMap = new HashMap<>();
    private final Project project;
    private final AtomicBoolean disposed = new AtomicBoolean();

    protected AbstractDialog(@Nullable Project project) {
        super(project);
        this.project = project;
    }

    public <T> T getComponent(Class<T> clazz, String key) {
        JComponent component = componentMap.get(key);
        if (component == null) {
            return null;
        }
        if (!clazz.isInstance(component)) {
            throw new IllegalStateException("对话框组件 " + key + " 的实际类型是 "
                    + component.getClass().getName() + "，不是 " + clazz.getName());
        }
        return clazz.cast(component);
    }

    public void addComponent(String key, JComponent component) {
        componentMap.put(key, component);
    }

    public @Nullable Project getProjectContext() {
        return project;
    }

    public boolean isDialogDisposed() {
        return disposed.get();
    }

    @Override
    protected void dispose() {
        disposed.set(true);
        super.dispose();
    }
}
