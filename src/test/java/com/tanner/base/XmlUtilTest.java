package com.tanner.base;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class XmlUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void restrictedDoctypeParserAcceptsLegacySpringDeclaration() throws Exception {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN"
                        "http://www.springframework.org/dtd/spring-beans.dtd">
                <beans/>
                """;

        Document document = XmlUtil.parseWithRestrictedDoctype(stream(xml));

        assertEquals("beans", document.getDocumentElement().getTagName());
    }

    @Test
    public void restrictedDoctypeParserDoesNotResolveExternalEntities() throws Exception {
        File secretFile = temporaryFolder.newFile("secret.txt");
        String secret = "must-not-be-read";
        Files.writeString(secretFile.toPath(), secret, StandardCharsets.UTF_8);
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE root [<!ENTITY xxe SYSTEM "%s">]>
                <root>&xxe;</root>
                """.formatted(secretFile.toURI());

        Document document = XmlUtil.parseWithRestrictedDoctype(stream(xml));

        assertFalse(document.getDocumentElement().getTextContent().contains(secret));
    }

    private ByteArrayInputStream stream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }
}
