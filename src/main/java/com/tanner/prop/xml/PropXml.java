package com.tanner.prop.xml;

import com.tanner.dbdriver.entity.DatabaseDriverSetInfo;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.PropInfo;

import java.io.File;

public class PropXml {

    private String xmlPath = "/bin/dbdriverset.xml";

    private String xmlPath2 = "/ierp/bin/dbdriverset.xml";

    public PropInfo loadPropInfo(String propfile) throws Exception {
        return (PropInfo) XMLToObject.getJavaObjectFromFile(propfile, PropInfo.class, true);
    }

    public PropInfo loadPropInfo(File propfile) throws Exception {
        return (PropInfo) XMLToObject.getJavaObjectFromFile(propfile, PropInfo.class, true);
    }

    public DataSourceMeta[] getDSMetaWithDesign(String propfile) throws Exception {
        DataSourceMeta[] metas = loadPropInfo(propfile).getDataSource();
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
        if (metas == null || metas.length == 0) {
            metaswithdesign[0] = new DataSourceMeta();
        } else {
            metaswithdesign[0] = metas[0];
            metaswithdesign[0].setDataSourceName("design");
        }
        return metaswithdesign;
    }

    public void saveMeta(String nchome, DataSourceMeta[] metas) throws Exception {
        PropInfo propinfo = loadPropInfo(nchome);
        propinfo.setDataSource(metas);
        storePorpInfo(nchome, propinfo);
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
