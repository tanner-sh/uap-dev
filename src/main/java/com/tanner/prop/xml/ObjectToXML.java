package com.tanner.prop.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

public class ObjectToXML {

    public static final String DOC_TYPE = "(Java lang)Middleware depoly parameter";
    public static Class<?>[] classA = {Boolean.class, Character.class, Integer.class, Long.class,
            Double.class, Float.class, String.class, java.math.BigDecimal.class, int.class, char.class,
            boolean.class, long.class, double.class, float.class};

    public static void saveAsXmlFile(String fileName, Object o) throws Exception {
        saveAsXmlFile(fileName, o, Object.class);
    }

    private static void saveAsXmlFile(String fileName, Object o, Class<?> defaultClass)
            throws Exception {
        Document doc = com.tanner.base.XmlUtil.newDocumentBuilder().newDocument();
        Element nod = doc.createElement("root");
        Node root = (new ObjectToXML()).getDocument(doc, nod, o, 0, defaultClass, null);
        doc.appendChild(root);
        Path output = Path.of(fileName).toAbsolutePath().normalize();
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "GB2312");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        try (var outputStream = Files.newOutputStream(output)) {
            transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        }
    }

    private void appendChild(Document doc, Node parent, Node child) {
        if (parent == null) {
            doc.appendChild(child);
        } else {
            parent.appendChild(child);
        }
    }

    private Class<?> getArrayItemClass(Class<?> arrayClass) throws Exception {
        if (arrayClass == null) {
            return null;
        }
        String className = arrayClass.getName();
        int key = className.indexOf("[L");
        if (key >= 0) {
            int lastLoc = className.indexOf(";");
            String classPureName = className.substring(key + 2, lastLoc);
            Class<?> pureClass = Class.forName(classPureName, false,
                    arrayClass.getClassLoader());
            if (key == 0) {
                return pureClass;
            }
            int[] arrayList = new int[key];
            for (int i = 0; i < arrayList.length; i++) {
                arrayList[i] = 1;
            }
            return Array.newInstance(pureClass, arrayList).getClass();
        }
        String[] id = {"[B", "[C", "[S", "[I", "[J", "[F", "[D", "[Z"};
        Class<?>[] type = {byte.class, char.class, short.class, int.class, long.class,
                float.class, double.class, boolean.class};
        for (int i = 0; i < id.length; i++) {
            key = className.indexOf(id[i]);
            if (key >= 0) {
                Class<?> pureClass = type[i];
                if (key == 0) {
                    return pureClass;
                }
                int[] arrayList = new int[key];
                for (int j = 0; j < arrayList.length; j++) {
                    arrayList[j] = 1;
                }
                return Array.newInstance(pureClass, arrayList).getClass();
            }
        }
        return Class.forName(className);
    }

    private Node getDocument(Document doc, Element nod, Object o, int deepSet,
                             Class<?> defaultClass,
                             String arrayName) throws Exception {
        int deep = deepSet + 1;
        if (o == null) {
            if (!defaultClass.isArray()) {
                nod.setAttribute("value", "null");
            } else {
                nod.setAttribute("arrayValue", "null");
            }
            return nod;
        }
        if (isPrimitive(o.getClass())) {
            nod.appendChild(doc.createTextNode(o.toString()));
            return nod;
        }
        if (o.getClass().isArray()) {
            int length = Array.getLength(o);
            Class<?> itemType = getArrayItemClass(defaultClass);
            for (int j = 0; j < length; j++) {
                if (arrayName == null) {
                    arrayName = "NODE";
                }
                Element arrayList = doc.createElement(arrayName);
                getDocument(doc, arrayList, Array.get(o, j), deep, itemType, arrayName);
                appendChild(doc, nod, arrayList);
            }
        } else {
            if (defaultClass != o.getClass()) {
                if (nod != null) {
                    nod.setAttribute("ClassType", o.getClass().getName());
                }
            }
            Field[] fa = o.getClass().getDeclaredFields();
            for (Field field : fa) {
                if (!Modifier.isFinal(field.getModifiers())) {
                    boolean wasAccessible = field.canAccess(o);
                    field.setAccessible(true);
                    try {
                    Element child = doc.createElement(field.getName());
                    Object oc = field.get(o);
                    if (oc != null && oc.getClass() != field.getType()
                            && !isPrimitive(oc.getClass())) {
                        child.setAttribute("ClassType", field.getType().getName());
                    }
                    if (oc == null) {
                        getDocument(doc, child, null, deep, field.getType(),
                                field.getName());
                        appendChild(doc, nod, child);
                    } else if (isPrimitive(oc.getClass())) {
                        child.appendChild(doc.createTextNode(oc.toString()));
                        appendChild(doc, nod, child);
                    } else if (oc.getClass().isArray()) {
                        getDocument(doc, nod, oc, deep, field.getType(),
                                field.getName());
                    } else {
                        getDocument(doc, child, oc, deep, field.getType(), null);
                        appendChild(doc, nod, child);
                    }
                    } finally {
                        field.setAccessible(wasAccessible);
                    }
                }
            }
        }
        return nod;
    }

    private boolean isPrimitive(Class<?> cl) {
        for (int i = 0; i < classA.length; i++) {
            if (classA[i] == cl) {
                return true;
            }
        }
        return false;
    }
}
