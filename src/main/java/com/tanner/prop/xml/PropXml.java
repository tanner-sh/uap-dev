package com.tanner.prop.xml;

import com.tanner.dbdriver.entity.DatabaseDriverSetInfo;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.PropInfo;

import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PropXml {

    private String xmlPath = "/bin/dbdriverset.xml";

    private String xmlPath2 = "/ierp/bin/dbdriverset.xml";

    public PropInfo loadPropInfo(String propfile) throws Exception {
        return (PropInfo) XMLToObject.getJavaObjectFromFile(propfile, PropInfo.class, true);
    }

    public PropInfo loadPropInfo(File propfile) throws Exception {
        return (PropInfo) XMLToObject.getJavaObjectFromFile(propfile, PropInfo.class, true);
    }

    public DataSourceMeta[] getDSMetaWithDesign(String propfile, String uapHomePath) throws Exception {
        DataSourceMeta[] metas = loadPropInfo(propfile).getDataSource(uapHomePath);
        if (metas == null || metas.length == 0) {
            return new DataSourceMeta[]{new DataSourceMeta()};
        }
        for (int i = 0; i < metas.length; i++) {
            DataSourceMeta meta = metas[i];
            if ("design".equals(meta.getDataSourceName())) {
                if (i != 0) {
                    DataSourceMeta tmp = metas[i];
                    metas[i] = metas[0];
                    metas[0] = tmp;
                }
                return metas;
            }
        }
        DataSourceMeta[] metaswithdesign = new DataSourceMeta[metas.length + 1];
        System.arraycopy(metas, 0, metaswithdesign, 1, metas.length);
        metaswithdesign[0] = (DataSourceMeta) metas[0].clone();
        metaswithdesign[0].setDataSourceName("design");
        metaswithdesign[0].setBase(false);
        return metaswithdesign;
    }

    public void saveMeta(String nchome, DataSourceMeta[] metas, String uapHomePath) throws Exception {
        PropInfo propinfo = loadPropInfo(nchome);
        propinfo.setDataSource(metas, uapHomePath);
        Path target = Path.of(nchome).toAbsolutePath().normalize();
        Path parent = target.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Invalid prop.xml path: " + nchome);
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        try {
            storePorpInfo(temporary.toString(), propinfo);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void storePorpInfo(String propfile, PropInfo propInfo) throws Exception {
        ObjectToXML.saveAsXmlFile(propfile, propInfo);
    }

    public DatabaseDriverSetInfo getDriverSet(String nchome) throws Exception {
        String fileName = nchome + this.xmlPath2;
        File file = new File(fileName);
        if (!file.exists()) {
            file = new File(nchome + this.xmlPath);
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("Configuration file not found");
        }
        return (DatabaseDriverSetInfo) XMLToObject.getJavaObjectFromFile(file,
                DatabaseDriverSetInfo.class, true);
    }

}
