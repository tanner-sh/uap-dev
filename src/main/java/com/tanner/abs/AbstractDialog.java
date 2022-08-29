package com.tanner.abs;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JDialog;

public abstract class AbstractDialog extends JDialog {

  private Map<String, JComponent> componentMap = new HashMap();

  public <T> T getComponent(Class<T> clazz, String key) {
    return (T) componentMap.get(key);
  }

  public void addComponent(String key, JComponent component) {
    componentMap.put(key, component);
  }
}
