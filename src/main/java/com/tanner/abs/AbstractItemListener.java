package com.tanner.abs;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * 下拉监听
 */
public abstract class AbstractItemListener implements ItemListener {

    private final AbstractDialog dialog;

    public AbstractItemListener(AbstractDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            afterSelect(e);
        }
    }

    public abstract void afterSelect(ItemEvent e);

    public AbstractDialog getDialog() {
        return dialog;
    }
}
