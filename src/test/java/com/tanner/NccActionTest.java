package com.tanner;

import com.tanner.actionsearch.entity.Action;
import com.tanner.actionsearch.entity.Actions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NccActionTest {

    @org.junit.Test
    public void getAllNccActions() throws Exception {
        String uapHome = "/Users/tanner/Documents/yonyou/uaphomes/NCC2005_all_modules";
        Path yyconfigPath = Paths.get(uapHome).resolve(Paths.get("hotwebs", "nccloud", "WEB-INF", "extend", "yyconfig", "modules"));
        Collection<File> xmlFiles = FileUtils.listFiles(yyconfigPath.toFile(), new String[]{"xml"}, true);
        IOFileFilter filter = new AndFileFilter(
                new SuffixFileFilter(".xml"),           // 后缀名为".xml"
                new IOFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getPath().contains("action");  // 路径包含"action"字符串
                    }

                    @Override
                    public boolean accept(File dir, String name) {
                        return true;  // 必须覆盖该方法
                    }
                }
        );
        List<File> actionFiles = xmlFiles.stream().filter(filter::accept).toList();
        List<Action> actionList = new ArrayList<>();
        for (File actionFile : actionFiles) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Actions.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Actions actions = (Actions) unmarshaller.unmarshal(actionFile);
                if (actions != null && CollectionUtils.isNotEmpty(actions.getActions())) {
                    actionList.addAll(actions.getActions());
                }
            } catch (Exception ignored) {

            }
        }
        actionList.forEach(System.out::println);
    }

}
