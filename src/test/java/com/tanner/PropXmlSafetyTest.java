package com.tanner;

import com.tanner.base.XmlUtil;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.PropInfo;
import com.tanner.prop.xml.ObjectToXML;
import com.tanner.prop.xml.PropXml;
import com.tanner.prop.xml.XMLToObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PropXmlSafetyTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void missingDesignUsesCloneAndPreservesOriginalDatasource() throws Exception {
        File home = temporaryFolder.newFolder("home");
        File propFile = new File(home, "ierp/bin/prop.xml");
        DataSourceMeta original = datasource("business", true);
        writeProp(propFile, home, original);

        PropXml propXml = new PropXml();
        DataSourceMeta[] loaded = propXml.getDSMetaWithDesign(propFile.getPath(), home.getPath());

        assertEquals(2, loaded.length);
        assertEquals("design", loaded[0].getDataSourceName());
        assertFalse(loaded[0].isBase());
        assertEquals("business", loaded[1].getDataSourceName());
        assertTrue(loaded[1].isBase());
    }

    @Test
    public void propSaveUsesDeclaredEncodingAndCanRoundTrip() throws Exception {
        File home = temporaryFolder.newFolder("中文-home");
        File propFile = new File(home, "ierp/bin/prop.xml");
        writeProp(propFile, home, datasource("design", false));

        new PropXml().saveMeta(propFile.getPath(),
                new DataSourceMeta[]{datasource("design", false),
                        datasource("业务库", true)}, home.getPath());

        String raw = new String(Files.readAllBytes(propFile.toPath()),
                Charset.forName("GB2312"));
        assertTrue(raw.contains("encoding=\"GB2312\""));
        DataSourceMeta[] loaded = new PropXml()
                .loadPropInfo(propFile).getDataSource(home.getPath());
        assertEquals("业务库", loaded[1].getDataSourceName());
    }

    @Test
    public void secureParserRejectsDoctypeAndFloatValuesRoundTrip() throws Exception {
        File malicious = temporaryFolder.newFile("xxe.xml");
        Files.writeString(malicious.toPath(), """
                <?xml version="1.0"?>
                <!DOCTYPE root [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <root>&xxe;</root>
                """);
        try {
            XmlUtil.parse(malicious);
            fail("DOCTYPE must be rejected");
        } catch (Exception expected) {
            assertTrue(expected.getMessage() != null);
        }

        File beanFile = temporaryFolder.newFile("float.xml");
        FloatBean bean = new FloatBean();
        bean.value = 1.25f;
        bean.values = new float[]{2.5f, 3.75f};
        ObjectToXML.saveAsXmlFile(beanFile.getPath(), bean);
        FloatBean loaded = (FloatBean) XMLToObject.getJavaObjectFromFile(
                beanFile, FloatBean.class, true);
        assertEquals(1.25f, loaded.value, 0.001f);
        assertEquals(3.75f, loaded.values[1], 0.001f);
    }

    private void writeProp(File propFile, File home, DataSourceMeta... metas)
            throws Exception {
        PropInfo info = new PropInfo();
        info.setDataSource(metas, home.getPath());
        ObjectToXML.saveAsXmlFile(propFile.getPath(), info);
    }

    private DataSourceMeta datasource(String name, boolean base) {
        DataSourceMeta meta = new DataSourceMeta();
        meta.setDataSourceName(name);
        meta.setBase(base);
        meta.setPassword("secret");
        return meta;
    }

    public static class FloatBean {
        public float value;
        public float[] values;

        public FloatBean() {
        }
    }
}
