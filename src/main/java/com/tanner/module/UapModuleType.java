package com.tanner.module;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleType;
import javax.swing.Icon;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class UapModuleType extends ModuleType<UapModuleBuilder> {

  public UapModuleType() {
    this("JAVA_MODULE");
  }

  protected UapModuleType(String id) {
    super(id);
  }


  @Override
  public @NotNull UapModuleBuilder createModuleBuilder() {
    return new UapModuleBuilder();
  }

  @Override
  public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getName() {
    return "ncc-dev-module";
  }

  @Override
  public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
    return "ncc-dev-module";
  }

  @Override
  public @NotNull Icon getNodeIcon(boolean b) {
    return AllIcons.Nodes.Module;
  }
}
