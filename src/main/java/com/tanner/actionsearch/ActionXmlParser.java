package com.tanner.actionsearch;

import com.tanner.actionsearch.entity.Action;
import com.tanner.base.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ActionXmlParser {

    private ActionXmlParser() {
    }

    public static List<Action> parse(File file) throws Exception {
        NodeList nodes = XmlUtil.parse(file).getElementsByTagName("action");
        List<Action> actions = new ArrayList<>(nodes.getLength());
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (!(node instanceof Element element)) {
                continue;
            }
            String name = value(element, "name");
            String label = value(element, "label");
            String clazz = value(element, "class");
            if (clazz.isBlank()) {
                clazz = value(element, "clazz");
            }
            actions.add(new Action(name, label, clazz));
        }
        return actions;
    }

    private static String value(Element element, String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name).trim();
        }
        NodeList children = element.getElementsByTagName(name);
        if (children.getLength() == 0) {
            return "";
        }
        return children.item(0).getTextContent().trim();
    }
}
