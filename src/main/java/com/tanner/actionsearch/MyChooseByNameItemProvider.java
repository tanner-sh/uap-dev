package com.tanner.actionsearch;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        List<NccActionItem> nccActionItems = new ArrayList<>();
        nccActionItems.add(new NccActionItem("epa.chartData.QueryCubedefContainTaskdefsAction", "查询应用模型并且包含下面的任务模板", "nccloud.web.epa.chartData.action.QueryCubedefContainTaskdefsAction"));
        nccActionItems.add(new NccActionItem("epa.chartData.QueryTasksEntRefAction", "通过选中的维度查询任务主体参照", "nccloud.web.epa.chartData.action.ref.QueryTasksEntRefAction"));
        nccActionItems.add(new NccActionItem("epa.chartData.QuerySheetsAndTaskdefDimsAction", "查询套表的sheets以及对应的任务参数维", "nccloud.web.epa.chartData.action.QuerySheetsAndTaskdefDimsAction"));
        cancelled.checkCanceled();
        if (!com.intellij.util.containers.ContainerUtil.process(nccActionItems, consumer)) {
            return false;
        }
        return nccActionItems.isEmpty();
    }
}
