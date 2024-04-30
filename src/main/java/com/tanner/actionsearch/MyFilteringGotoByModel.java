package com.tanner.actionsearch;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MyFilteringGotoByModel extends FilteringGotoByModel<NccActionItem> {

    public MyFilteringGotoByModel(@NotNull Project project, ChooseByNameContributor @NotNull [] contributors) {
        super(project, contributors);
    }

    public MyFilteringGotoByModel(@NotNull Project project, @NotNull List<ChooseByNameContributor> contributors) {
        super(project, contributors);
    }

    @Override
    public @NotNull ChooseByNameItemProvider getItemProvider(@Nullable PsiElement context) {
        return new MyChooseByNameItemProvider();
    }

    @Override
    protected @Nullable NccActionItem filterValueFor(NavigationItem item) {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "Search action";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotInMessage() {
        return "";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotFoundMessage() {
        return "Not found";
    }

    @Override
    public @Nullable @NlsContexts.Label String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {

    }

    @Override
    public String @NotNull [] getSeparators() {
        return new String[0];
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return null;
    }

    @Override
    public boolean willOpenEditor() {
        return false;
    }

}
