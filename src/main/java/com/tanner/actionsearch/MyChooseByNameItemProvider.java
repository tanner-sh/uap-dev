package com.tanner.actionsearch;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Processor;
import com.tanner.actionsearch.entity.Action;
import com.tanner.actionsearch.entity.Actions;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.XmlUtil;
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
import java.util.Locale;

public class MyChooseByNameItemProvider implements ChooseByNameItemProvider {
    private List<NccActionItem> cachedItems;

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
        String normalizedPattern = pattern.toLowerCase(Locale.ROOT);
        if (cachedItems == null) {
            cachedItems = getAllNccActionItems(base.getProject(), cancelled);
        }
        List<NccActionItem> nccActionItems = cachedItems.stream()
                .filter(nccActionItem -> matches(nccActionItem, normalizedPattern))
                .toList();
        cancelled.checkCanceled();
        return com.intellij.util.containers.ContainerUtil.process(nccActionItems, consumer);
    }

    private boolean matches(NccActionItem item, String pattern) {
        return containsIgnoreCase(item.getName(), pattern)
                || containsIgnoreCase(item.getLabel(), pattern)
                || containsIgnoreCase(item.getClazz(), pattern);
    }

    private boolean containsIgnoreCase(String value, String normalizedPattern) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedPattern);
    }

    private List<NccActionItem> getAllNccActionItems(
            Project project, ProgressIndicator indicator) {
        List<NccActionItem> returnList = new ArrayList<>();
        if (project == null) {
            return returnList;
        }
        UapProjectEnvironment instance = UapProjectEnvironment.getInstance(project);
        if (instance == null) {
            Messages.showMessageDialog("Please open a project", "Error", Messages.getErrorIcon());
            return returnList;
        }
        String uapHomePath = instance.getUapHomePath();
        if (uapHomePath == null || uapHomePath.isBlank()) {
            return returnList;
        }
        Path yyconfigPath = Paths.get(uapHomePath).resolve(Paths.get("hotwebs", "nccloud", "WEB-INF", "extend", "yyconfig", "modules"));
        if (!yyconfigPath.toFile().exists()) {
            return returnList;
        }
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
        List<Action> actionList = new ArrayList<>();
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Actions.class);
        } catch (Exception exception) {
            return returnList;
        }
        for (File actionFile : xmlFiles) {
            indicator.checkCanceled();
            if (!filter.accept(actionFile)) {
                continue;
            }
            try {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Actions actions = (Actions) unmarshaller.unmarshal(XmlUtil.parse(actionFile));
                if (actions != null && actions.getActions() != null
                        && !actions.getActions().isEmpty()) {
                    actionList.addAll(actions.getActions());
                }
            } catch (Exception ignored) {

            }
        }
        actionList.forEach(action -> returnList.add(new NccActionItem(action.getName(), action.getLabel(), action.getClazz())));
        return returnList;
    }

}
