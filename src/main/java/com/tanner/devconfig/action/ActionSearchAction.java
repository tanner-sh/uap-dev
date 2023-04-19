package com.tanner.devconfig.action;

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.tanner.actionsearch.MyFilteringGotoByModel;
import com.tanner.actionsearch.NccActionItem;
import com.tanner.base.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ActionSearchAction extends GotoActionBase {

    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        MyFilteringGotoByModel myFilteringGotoByModel = new MyFilteringGotoByModel(e.getProject(), new ArrayList<>());
        showNavigationPopup(e, myFilteringGotoByModel, new MyGotoActionCallback(e));
    }

    static class MyGotoActionCallback extends GotoActionBase.GotoActionCallback<NccActionItem> {

        private AnActionEvent anActionEvent;

        public MyGotoActionCallback() {
        }

        public MyGotoActionCallback(AnActionEvent anActionEvent) {
            this.anActionEvent = anActionEvent;
        }

        @Override
        public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object element) {
            if (element instanceof NccActionItem) {
                Project project = ProjectManager.getInstance().getProject();
                SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(project);
                FeatureUsageTracker.getInstance().triggerFeatureUsed("SearchEverywhere");
                IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
                seManager.show("ClassSearchEverywhereContributor", ((NccActionItem) element).getClazz(), anActionEvent);
            }
        }

    }

}
