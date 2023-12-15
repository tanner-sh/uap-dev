package com.tanner.devconfig.action.button;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.UapUtil;
import com.tanner.debug.util.CreatApplicationConfigurationUtil;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.devconfig.util.TableModelUtil;
import com.tanner.library.action.LibrariesUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * 设置面板确认按钮
 */
public class OKAction extends AbstractButtonAction {

    public OKAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        boolean homeChanged = false;
        String homePath = getDialog().getComponent(JTextField.class, "homeText").getText();
        if (StringUtils.isBlank(homePath)) {
            return;
        }
        //如果homePath未发生变化，则不提示是否更新类路径
        if (StringUtils.isNotBlank(homePath) && !homePath.equals(
                UapProjectEnvironment.getInstance().getUapHomePath())) {
            homeChanged = true;
        }
        setServiceConfig(homeChanged);
        setLibraries(homeChanged);
        saveDataSource(homeChanged);
        saveModuleConfig(homeChanged);
        getDialog().close(0);
    }

    /**
     * 保存启动module
     *
     * @param homeChanged
     */
    private void saveModuleConfig(boolean homeChanged) throws BusinessException {
        TableModelUtil.saveModuleConfig(getDialog());
    }

    /**
     * 保存工程依赖
     *
     * @param homeChanged
     */
    private void setLibraries(boolean homeChanged) throws BusinessException {
        boolean setLibFlag = ((DevConfigDialog) getDialog()).isLibFlag();
        if (!homeChanged || setLibFlag) {
            return;
        }
        String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        int opt = Messages.showYesNoDialog("是否更新类路径？", "询问", Messages.getQuestionIcon());
        if (opt == Messages.OK) {
            try {
                LibrariesUtil.setLibraries(homePath);
            } catch (BusinessException e) {
                Messages.showErrorDialog(e.getMessage(), "出错了");
            }
        }
        //更新application上的home路径
        CreatApplicationConfigurationUtil.update();
    }


    /**
     * 保存数据源
     */
    private void saveDataSource(boolean homeChanged) {
        DataSourceUtil.saveDesignDataSourceMeta((DevConfigDialog) getDialog());
    }


    /**
     * 保存home
     */
    private void setServiceConfig(boolean homeChanged) {
        String homePath = getDialog().getComponent(JTextField.class, "homeText").getText();
        UapProjectEnvironment.getInstance().setUapHomePath(homePath);
        UapProjectEnvironment.getInstance().setUapVersion(UapUtil.getUapVersion(homePath));
    }
}
