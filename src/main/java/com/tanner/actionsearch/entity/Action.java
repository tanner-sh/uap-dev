package com.tanner.actionsearch.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Action {

    private String name;
    private String label;
    private String clazz;

    public Action() {
    }

    public Action(String name, String label, String clazz) {
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
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", clazz='" + clazz + '\'' +
                '}';
    }
}
