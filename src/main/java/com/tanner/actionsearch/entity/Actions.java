package com.tanner.actionsearch.entity;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Actions {

    @XmlElement(name = "action")
    private List<Action> actions;

    public Actions() {
    }

    public Actions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return "Actions{" +
                "actions=" + actions +
                '}';
    }
}
