package com.tanner.debug.util;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.LanguageLevelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.tanner.base.BaseUtil;
import com.tanner.base.BusinessException;
import com.tanner.base.ProjectManager;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.prop.entity.ClusterInfo;
import com.tanner.prop.entity.DomainInfo;
import com.tanner.prop.entity.IpAndPort;
import com.tanner.prop.entity.SingleServerInfo;
import com.tanner.prop.xml.PropXml;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

import static com.intellij.execution.ShortenCommandLine.CLASSPATH_FILE;

public class CreatApplicationConfigurationUtil {

    public static final int DEFALUT_PORT = 80;
    public static final String DEFALUT_IP = "127.0.0.1";
    private static final String serverClass = "ufmiddle.start.tomcat.StartDirectServer";
    private static final String clientClass = "nc.starter.test.JStarter";

    /**
     * 设置启动application
     *
     * @param event      event
     * @param serverFlag serverFlag
     */
    public static void createApplicationConfiguration(AnActionEvent event, boolean serverFlag)
            throws BusinessException {
        String configName = serverFlag ? " - server" : " - client";
        Project project = BaseUtil.getProject(event);
        RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
        List<RunConfiguration> configurationsList = runManager.getAllConfigurationsList();
        //当前选择module
        Module selectModule = BaseUtil.getModule(event);
        configName = selectModule.getName() + configName;
        //先删除已经存在的配置
        if (CollectionUtils.isNotEmpty(configurationsList)) {
            for (RunConfiguration configuration : configurationsList) {
                if (configuration.getName().equals(configName)) {
                    runManager.removeConfiguration(
                            new RunnerAndConfigurationSettingsImpl(runManager, configuration));
                }
            }
        }
        //新增config
        ApplicationConfiguration conf = new ApplicationConfiguration(configName, project,
                ApplicationConfigurationType.getInstance());
        setConfiguration(selectModule, conf, serverFlag);
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl(
                runManager, conf);
        runManager.addConfiguration(runnerAndConfigurationSettings);
        runManager.setSelectedConfiguration(runnerAndConfigurationSettings);
    }

    private static void setConfiguration(Module selectModule, ApplicationConfiguration conf,
                                         boolean serverFlag) throws BusinessException {
        //检查并设置nc home
        String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        if (StringUtils.isBlank(homePath)) {
            throw new BusinessException("请先设置NC Home");
        }
        PropXml propXml = new PropXml();
        String filename = new File(homePath).getPath() + "/ierp/bin/prop.xml";
        File file = new File(filename);
        if (!file.exists()) {
            throw new BusinessException("file :prop.xml not exists!");
        }
        LanguageLevel languageLevel = LanguageLevelUtil.getEffectiveLanguageLevel(selectModule);
        int feature = languageLevel.toJavaVersion().feature;
        if (serverFlag) {
            conf.setMainClassName(serverClass);
            String exModules = UapProjectEnvironment.getInstance().getEx_modules();
            conf.setVMParameters(getDefalutsServerVMParameters(feature, exModules, homePath));
        } else {
            // ip和端口号读取home中的，没有就取默认值127.0.0.1:80
            IpAndPort ipAndPort = new IpAndPort();
            ipAndPort.setAddress(DEFALUT_IP);
            ipAndPort.setPort(DEFALUT_PORT);
            try {
                DomainInfo domainInfo = propXml.loadPropInfo(file).getDomain();
                SingleServerInfo serverInfo = domainInfo.getServer();
                //如果severInfo拿不到，尝试判断是集群配置，获取主服务配置
                if (serverInfo == null) {
                    ClusterInfo clusterInfo = domainInfo.getCluster();
                    if (clusterInfo != null) {
                        serverInfo = clusterInfo.getMgr();
                    }
                }
                if (serverInfo != null) {
                    // 优先http，然后是https，然后是ajp
                    if (ArrayUtils.isNotEmpty(serverInfo.getHttp())) {
                        ipAndPort = serverInfo.getHttp()[0];
                    } else if (ArrayUtils.isNotEmpty(serverInfo.getHttps())) {
                        ipAndPort = serverInfo.getHttps()[0];
                    } else if (ArrayUtils.isNotEmpty(serverInfo.getAjp())) {
                        ipAndPort = serverInfo.getAjp()[0];
                    }
                }
            } catch (Exception e) {
                throw new BusinessException("please check the file :prop.xml\n" + e.getMessage());
            }
            //本地调试移动用127.0.0.1
            ipAndPort.setAddress(DEFALUT_IP);
            String clientIp = StringUtils.isBlank(ipAndPort.getAddress()) ? DEFALUT_IP : ipAndPort.getAddress();
            String clientPort = String.valueOf(ipAndPort.getPort() == null ? DEFALUT_PORT : ipAndPort.getPort());
            conf.setMainClassName(clientClass);
            conf.setVMParameters(getDefalutsClientVMParameters(clientIp, clientPort));
        }
        conf.setModule(selectModule);
        conf.setWorkingDirectory(homePath);
        conf.setShortenCommandLine(CLASSPATH_FILE);
    }

    /**
     * 更新application
     *
     * @throws BusinessException BusinessException
     */
    public static void update() throws BusinessException {
        Project project = ProjectManager.getInstance().getProject();
        RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(project);
        List<RunConfiguration> configurationsList = runManager.getAllConfigurationsList();
        if (!configurationsList.isEmpty()) {
            for (RunConfiguration configuration : configurationsList) {
                if (configuration instanceof ApplicationConfiguration conf) {
                    RunnerAndConfigurationSettings runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl(
                            runManager, conf);
                    runManager.removeConfiguration(runnerAndConfigurationSettings);
                    ApplicationConfiguration newConf = (ApplicationConfiguration) conf.clone();
                    setConfiguration(newConf.getConfigurationModule().getModule(), newConf,
                            serverClass.equals(newConf.getMainClassName()));
                    runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl(runManager,
                            newConf);
                    runManager.addConfiguration(runnerAndConfigurationSettings);
                }
            }
        }
    }

    private static String getDefalutsServerVMParameters(int feature, String exModules, String homePath) {
        StringBuilder parameters = new StringBuilder();
        parameters.append("-Dnc.exclude.modules=").append(exModules).append("\n");
        parameters.append("-Dnc.runMode=develop\n");
        parameters.append("-Dnc.server.location=").append(homePath).append("\n");
        parameters.append("-DEJBConfigDir=").append(homePath).append("/ejbXMLs\n");
        parameters.append("-DExtServiceConfigDir=").append(homePath).append("/ejbXMLs\n");
        parameters.append("-Duap.hotwebs=").append("nccloud,fs,fbip").append("\n");
        parameters.append("-Duap.disable.codescan=false\n");
        parameters.append("-Dorg.owasp.esapi.resources=").append(homePath).append("/ierp/bin/esapi\n");
        parameters.append("-Dfile.encoding=").append("GB2312").append("\n"); // 默认编码
        parameters.append("-Duser.timezone=").append("GMT+8").append("\n");// 默认时区
        if (feature >= 8) {//jdk8以上
            parameters.append("-Xmx1024m\n");
            parameters.append("-XX:MetaspaceSize=128m\n");
            parameters.append("-XX:MaxMetaspaceSize=512m\n");
        } else {
            parameters.append("-Xms512m\n");
            parameters.append("-Xmx1024m\n");
            parameters.append("-XX:MaxPermSize=128m\n");
        }
        return parameters.toString();
    }

    private static String getDefalutsClientVMParameters(String clientIp, String clientPort) {
        StringBuilder parameters = new StringBuilder();
        parameters.append("-Dnc.runMode=develop\n");
        parameters.append("-Dnc.jstart.server=").append(clientIp).append("\n");
        parameters.append("-Dnc.jstart.port=").append(clientPort).append("\n");
        parameters.append("-Xmx768m -XX:MaxPermSize=256m\n");
        parameters.append("-Dnc.fi.autogenfile=N\n");
        return parameters.toString();
    }

}
