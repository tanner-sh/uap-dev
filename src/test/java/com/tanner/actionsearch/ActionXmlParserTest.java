package com.tanner.actionsearch;

import com.tanner.actionsearch.entity.Action;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ActionXmlParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parsesAttributeAndElementActionFormats() throws Exception {
        File xml = temporaryFolder.newFile("actions.xml");
        Files.writeString(xml.toPath(), """
                <actions>
                  <action name="open" label="打开" class="demo.OpenAction"/>
                  <action>
                    <name>close</name>
                    <label>关闭</label>
                    <clazz>demo.CloseAction</clazz>
                  </action>
                </actions>
                """);

        List<Action> actions = ActionXmlParser.parse(xml);

        assertEquals(2, actions.size());
        assertEquals("打开", actions.get(0).getLabel());
        assertEquals("demo.OpenAction", actions.get(0).getClazz());
        assertEquals("demo.CloseAction", actions.get(1).getClazz());
    }

    @Test
    public void rejectsExternalEntities() throws Exception {
        File xml = temporaryFolder.newFile("xxe-actions.xml");
        Files.writeString(xml.toPath(), """
                <!DOCTYPE actions [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <actions><action name="&xxe;"/></actions>
                """);
        try {
            ActionXmlParser.parse(xml);
            fail("DOCTYPE must be rejected");
        } catch (Exception expected) {
            assertTrue(expected.getMessage() != null);
        }
    }
}
