package com.tanner.actionsearch;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NccActionItem implements NavigationItem {

    private String name;
    private String label;
    private String clazz;

    public NccActionItem(String name, String label, String clazz) {
        this.name = name;
        this.label = label;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @NlsSafe @Nullable String getPresentableText() {
                return getClazz();
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return null;
            }
        };
    }

    @Override
    public void navigate(boolean requestFocus) {

    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public String toString() {
        return "NccActionItem{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", clazz='" + clazz + '\'' +
                '}';
    }
}
