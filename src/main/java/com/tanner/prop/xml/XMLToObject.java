package com.tanner.prop.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class XMLToObject {

    public static Class<?>[] classA = {Boolean.class, Character.class, Integer.class, Long.class,
            Double.class, Float.class, String.class, BigDecimal.class, int.class, char.class,
            boolean.class, long.class, double.class, float.class};

    private boolean m_AllowNoField = false;

    public static Object getJavaObjectFromFile(File file, Class<?> rootClass,
                                               boolean allowNoField)
            throws Exception {
        Document doc = com.tanner.base.XmlUtil.parse(file);
        return getJavaObjectFromDocument(doc, rootClass, allowNoField);
    }

    public static Object getJavaObjectFromDocument(Document doc, Class<?> rootClass,
                                                   boolean allowNoField) throws Exception {
        XMLToObject xto = new XMLToObject();
        xto.setAllowNoField(allowNoField);
        Node node = doc.getDocumentElement();
        return xto.revertDocument(node, rootClass, null);
    }

    public static Object getJavaObjectFromFile(String fileName, Class<?> rootClass,
                                               boolean allowNoField)
            throws Exception {
        return getJavaObjectFromFile(new File(fileName), rootClass, allowNoField);
    }

    private void fillFieldValue(Field f, Object o, String str) throws Exception {
        Class<?> itemClass = f.getType();
        String value = str.trim();
        boolean isObjectPrimitiveClass = false;
        Class<?>[] classA = {Boolean.class, Character.class, Integer.class, Long.class, String.class,
                Double.class, Float.class, BigDecimal.class};
        for (int i = 0; i < classA.length; i++) {
            if (classA[i] == itemClass) {
                isObjectPrimitiveClass = true;
                break;
            }
        }
        Object itemValue = null;
        if (!value.equals("null") && isObjectPrimitiveClass) {
            if (itemClass == String.class) {
                itemValue = value;
            } else if (itemClass == Integer.class) {
                itemValue = Integer.valueOf(value);
            } else if (itemClass == Boolean.class) {
                itemValue = Boolean.valueOf(value);
            } else if (itemClass == Character.class) {
                itemValue = Character.valueOf(value.charAt(0));
            } else if (itemClass == Long.class) {
                itemValue = Long.valueOf(value);
            } else if (itemClass == Double.class) {
                itemValue = Double.valueOf(value);
            } else if (itemClass == Float.class) {
                itemValue = Float.valueOf(value);
            } else if (itemClass == BigDecimal.class) {
                itemValue = new BigDecimal(value);
            }
        }
        if (isObjectPrimitiveClass) {
            f.set(o, itemValue);
            return;
        }
        if (itemClass == int.class) {
            f.setInt(o, Integer.parseInt(value));
        } else if (itemClass == boolean.class) {
            f.setBoolean(o, Boolean.valueOf(value).booleanValue());
        } else if (itemClass == char.class) {
            f.setChar(o, value.charAt(0));
        } else if (itemClass == long.class) {
            f.setLong(o, Long.parseLong(value));
        } else if (itemClass == double.class) {
            f.setDouble(o, Double.valueOf(value).doubleValue());
        } else if (itemClass == float.class) {
            f.setFloat(o, Float.parseFloat(value));
        }
    }

    private Class<?> getArrayItemClass(String className, ClassLoader loader) throws Exception {
        int key = className.indexOf("[L");
        if (key >= 0) {
            int lastLoc = className.indexOf(";");
            String classPureName = className.substring(key + 2, lastLoc);
            Class<?> pureClass;
            if (loader != null) {
                pureClass = loader.loadClass(classPureName);
            } else {
                pureClass = Class.forName(classPureName);
            }
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
        if (loader != null) {
            return loader.loadClass(className);
        }
        return Class.forName(className);
    }

    private boolean isAllowNoField() {
        return this.m_AllowNoField;
    }

    public void setAllowNoField(boolean newAllowNoField) {
        this.m_AllowNoField = newAllowNoField;
    }

    private boolean isArrayClass(String classArrayName) throws Exception {
        return classArrayName.startsWith("[");
    }

    private boolean isNullNode(Node cNode) {
        Node valueNode = cNode.getAttributes().getNamedItem("value");
        if (valueNode == null) {
            return false;
        }
        if (valueNode.getNodeValue().equals("null")) {
            return true;
        }
        return false;
    }

    private boolean isNullNodeArray(Node cNode) {
        Node valueNode = cNode.getAttributes().getNamedItem("arrayValue");
        if (valueNode == null) {
            return false;
        }
        if (valueNode.getNodeValue().equals("null")) {
            return true;
        }
        return false;
    }

    private boolean isPrimitive(Class<?> cl) {
        for (int i = 0; i < classA.length; i++) {
            if (classA[i] == cl) {
                return true;
            }
        }
        return false;
    }

    private Object revertArray(Node cNode, Class<?> defautClass, String nodeName)
            throws Exception {
        String arrayName = defautClass.getName();
        ClassLoader loader = defautClass.getClassLoader();
        if (defautClass.isArray()) {
            loader = defautClass.getComponentType().getClassLoader();
        }
        Class<?> arrayItemClass = getArrayItemClass(arrayName, loader);
        NodeList nl = cNode.getChildNodes();
        List<Node> matchingNodes = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(nodeName)) {
                matchingNodes.add(nl.item(i));
            }
        }
        if (matchingNodes.size() == 1 && isNullNodeArray(matchingNodes.getFirst())) {
            return null;
        }
        Object o = Array.newInstance(arrayItemClass, matchingNodes.size());
        if (isPrimitive(arrayItemClass)) {
            for (int i = 0; i < matchingNodes.size(); i++) {
                Node item = matchingNodes.get(i);
                String str = item.getChildNodes().item(0).getNodeValue().trim();
                setArrayPrimitiveValue(o, i, arrayItemClass, str);
            }
        } else {
            for (int i = 0; i < matchingNodes.size(); i++) {
                Node item = matchingNodes.get(i);
                Array.set(o, i, revertDocument(item, arrayItemClass, nodeName));
            }
        }
        return o;
    }

    private Object revertDocument(Node item, Class<?> defaultClass, String nodeName)
            throws Exception {
        if (isNullNode(item)) {
            return null;
        }
        String className = defaultClass.getName();
        if (isArrayClass(className)) {
            return revertArray(item, defaultClass, nodeName);
        }
        ClassLoader loader = defaultClass.getClassLoader();
        Class<?> classType = loader == null
                ? Class.forName(className)
                : loader.loadClass(className);
        Object o = newInstance(classType);
        if (isPrimitive(classType)) {
            throw new Exception("Parse Error");
        }
        NodeList nl = item.getChildNodes();
        Field[] fa = o.getClass().getDeclaredFields();
        for (Field field : fa) {
            if (!Modifier.isFinal(field.getModifiers())) {
                boolean wasAccessible = field.canAccess(o);
                field.setAccessible(true);
                try {
                Class<?> c = field.getType();
                Node cNode = null;
                for (int j = 0; j < nl.getLength(); j++) {
                    if (field.getName().equals(nl.item(j).getNodeName())) {
                        cNode = nl.item(j);
                        break;
                    }
                }
                if (cNode == null && !field.getType().isArray()) {
                    if (!isAllowNoField()) {
                        String msg = MessageFormat.format("Description of {0} lost",
                                field.getName());
                        throw new Exception(msg);
                    }
                } else {
                    if (field.getType().isArray()) {
                        Object oa = revertArray(item, field.getType(), field.getName());
                        field.set(o, oa);
                    } else if (isNullNode(cNode)) {
                        if (!field.getType().isPrimitive()) {
                            field.set(o, null);
                        }
                    } else if (isPrimitive(c)) {
                        NodeList nlc = cNode.getChildNodes();
                        if (nlc.item(0) == null) {
                            fillFieldValue(field, o, "");
                        } else {
                            fillFieldValue(field, o, nlc.item(0).getNodeValue());
                        }
                    } else {
                        Object os = revertDocument(cNode, field.getType(), nodeName);
                        field.set(o, os);
                    }
                }
                } finally {
                    field.setAccessible(wasAccessible);
                }
            }
        }
        return o;
    }

    private <T> T newInstance(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        boolean wasAccessible = constructor.canAccess(null);
        constructor.setAccessible(true);
        try {
            return constructor.newInstance();
        } finally {
            constructor.setAccessible(wasAccessible);
        }
    }

    private void setArrayPrimitiveValue(Object arrayObject, int location, Class<?> itemClass,
                                        String value) throws Exception {
        boolean isObjectPrimitiveClass = false;
        Class<?>[] classA = {Boolean.class, Character.class, Integer.class, Long.class, String.class,
                Double.class, Float.class, BigDecimal.class};
        for (int i = 0; i < classA.length; i++) {
            if (classA[i] == itemClass) {
                isObjectPrimitiveClass = true;
                break;
            }
        }
        Object itemValue = null;
        if (!value.equals("null") && isObjectPrimitiveClass) {
            if (itemClass == String.class) {
                itemValue = value;
            } else if (itemClass == Integer.class) {
                itemValue = Integer.valueOf(value);
            } else if (itemClass == Boolean.class) {
                itemValue = Boolean.valueOf(value);
            } else if (itemClass == Character.class) {
                itemValue = Character.valueOf(value.charAt(0));
            } else if (itemClass == Long.class) {
                itemValue = Long.valueOf(value);
            } else if (itemClass == Double.class) {
                itemValue = Double.valueOf(value);
            } else if (itemClass == Float.class) {
                itemValue = Float.valueOf(value);
            } else if (itemClass == BigDecimal.class) {
                itemValue = new BigDecimal(value);
            }
        }
        if (isObjectPrimitiveClass) {
            Array.set(arrayObject, location, itemValue);
            return;
        }
        if (itemClass == int.class) {
            Array.setInt(arrayObject, location, Integer.parseInt(value));
        } else if (itemClass == boolean.class) {
            Array.setBoolean(arrayObject, location, Boolean.valueOf(value).booleanValue());
        } else if (itemClass == char.class) {
            Array.setChar(arrayObject, location, value.charAt(0));
        } else if (itemClass == long.class) {
            Array.setLong(arrayObject, location, Long.parseLong(value));
        } else if (itemClass == double.class) {
            Array.setDouble(arrayObject, location, Double.valueOf(value).doubleValue());
        } else if (itemClass == float.class) {
            Array.setFloat(arrayObject, location, Float.parseFloat(value));
        }
    }
}
