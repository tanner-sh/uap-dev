package com.tanner.actionsearch;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import com.tanner.actionsearch.entity.Action;
import com.tanner.actionsearch.entity.Actions;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MyChooseByNameItemProvider implements ChooseByNameItemProvider {
    @Override
    public @NotNull List<String> filterNames(
            @NotNull ChooseByNameViewModel base,
            String @NotNull [] names,
            @NotNull String pattern) {
        return new ArrayList<>();
    }

    @Override
    public boolean filterElements(
            @NotNull ChooseByNameViewModel base,
            @NotNull String pattern,
            boolean everywhere,
            @NotNull ProgressIndicator cancelled,
            @NotNull Processor<Object> consumer) {
        List<NccActionItem> nccActionItems = getAllNccActionItems(base.getProject())
                .stream()
                .filter(nccActionItem -> nccActionItem.toString().contains(pattern))
                .collect(Collectors.toList());
        cancelled.checkCanceled();
        if (!com.intellij.util.containers.ContainerUtil.process(nccActionItems, consumer)) {
            return false;
        }
        return nccActionItems.isEmpty();
    }

    private List<NccActionItem> getAllNccActionItems(Project project) {
        List<NccActionItem> returnList = new ArrayList<>();
        String uapHomePath = UapProjectEnvironment.getInstance(project).getUapHomePath();
        Path yyconfigPath = Paths.get(uapHomePath).resolve(Paths.get("hotwebs", "nccloud", "WEB-INF", "extend", "yyconfig", "modules"));
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
        actionList.forEach(action -> returnList.add(new NccActionItem(action.getName(), action.getLabel(), action.getClazz())));
        return returnList;
    }

}
