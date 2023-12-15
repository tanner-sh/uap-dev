package com.tanner.abs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDialog extends DialogWrapper {

    private Map<String, JComponent> componentMap = new HashMap<>();

    protected AbstractDialog(@Nullable Project project) {
        super(project);
    }

    public <T> T getComponent(Class<T> clazz, String key) {
        return (T) componentMap.get(key);
    }

    public void addComponent(String key, JComponent component) {
        componentMap.put(key, component);
    }
}
