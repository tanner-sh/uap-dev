package com.tanner.base;

import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;

public final class XmlUtil {

    private XmlUtil() {
    }

    public static Document parse(File file) throws Exception {
        return newDocumentBuilder().parse(file);
    }

    public static Document parse(InputStream inputStream) throws Exception {
        return newDocumentBuilder().parse(inputStream);
    }

    /**
     * Parses legacy XML that declares a DOCTYPE without allowing the parser to
     * load external DTDs or expand external entities.
     */
    public static Document parseWithRestrictedDoctype(InputStream inputStream) throws Exception {
        return newDocumentBuilder(true).parse(inputStream);
    }

    public static DocumentBuilder newDocumentBuilder() throws Exception {
        return newDocumentBuilder(false);
    }

    private static DocumentBuilder newDocumentBuilder(boolean allowDoctype) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", !allowDoctype);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }
}
