package com.tanner.devconfig.util;

import com.tanner.base.BusinessException;
import com.tanner.prop.entity.DataSourceMeta;
import org.junit.Test;

import javax.swing.JComboBox;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DataSourceUtilTest {

    @Test
    public void collectingDatasourcesPreservesPoolSettings() throws Exception {
        DataSourceMeta meta = new DataSourceMeta();
        meta.setDataSourceName("business");
        meta.setMaxCon(17);
        meta.setMinCon(4);
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"business"});

        DataSourceMeta[] collected = DataSourceUtil.collectDataSourcesForSave(
                comboBox, Map.of("business", meta));

        assertEquals(17, collected[0].getMaxCon());
        assertEquals(4, collected[0].getMinCon());
    }

    @Test
    public void collectingDatasourcesRejectsOutOfSyncUiState() throws Exception {
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"missing"});
        try {
            DataSourceUtil.collectDataSourcesForSave(comboBox, Map.of());
            fail("Missing datasource should fail");
        } catch (BusinessException expected) {
            assertEquals("数据源列表与配置不一致: missing", expected.getMessage());
        }
    }
}
