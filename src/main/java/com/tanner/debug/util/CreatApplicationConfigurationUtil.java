package com.tanner.debug.util;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.LanguageLevelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.tanner.base.BaseUtil;
import com.tanner.base.BusinessException;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.prop.entity.ClusterInfo;
import com.tanner.prop.entity.DomainInfo;
import com.tanner.prop.entity.IpAndPort;
import com.tanner.prop.entity.SingleServerInfo;
import com.tanner.prop.xml.PropXml;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

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
        RunManager runManager = RunManager.getInstance(project);
        //当前选择module
        Module selectModule = BaseUtil.getModule(event);
        if (selectModule == null) {
            throw new BusinessException("请选择模块");
        }
        configName = selectModule.getName() + configName;
        RunnerAndConfigurationSettings existing = runManager.findConfigurationByName(configName);
        String expectedMainClass = serverFlag ? serverClass : clientClass;
        if (existing != null && (!(existing.getConfiguration()
                instanceof ApplicationConfiguration existingApplication)
                || !expectedMainClass.equals(existingApplication.getMainClassName()))) {
            throw new BusinessException("已存在同名的非 UAP 运行配置: " + configName);
        }
        // 先完整创建新配置，验证成功后才替换旧配置。
        RunnerAndConfigurationSettings newSettings = runManager.createConfiguration(configName,
                ApplicationConfigurationType.class);
        ApplicationConfiguration conf =
                (ApplicationConfiguration) newSettings.getConfiguration();
        setConfiguration(project, selectModule, conf, serverFlag);
        if (existing != null) {
            runManager.removeConfiguration(existing);
        }
        runManager.addConfiguration(newSettings);
        runManager.setSelectedConfiguration(newSettings);
    }

    private static void setConfiguration(Project project, Module selectModule,
                                         ApplicationConfiguration conf,
                                         boolean serverFlag) throws BusinessException {
        //检查并设置nc home
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(project);
        String homePath = environment.getUapHomePath();
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
            String exModules = environment.getEx_modules();
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
            conf.setVMParameters(getDefalutsClientVMParameters(feature, clientIp, clientPort));
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
    public static void update(Project project) throws BusinessException {
        RunManager runManager = RunManager.getInstance(project);
        for (RunnerAndConfigurationSettings settings : runManager.getAllSettings()) {
            RunConfiguration configuration = settings.getConfiguration();
            if (configuration instanceof ApplicationConfiguration conf
                    && (serverClass.equals(conf.getMainClassName())
                    || clientClass.equals(conf.getMainClassName()))) {
                Module module = conf.getConfigurationModule().getModule();
                if (module != null) {
                    setConfiguration(project, module, conf,
                            serverClass.equals(conf.getMainClassName()));
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

    private static String getDefalutsClientVMParameters(int feature, String clientIp,
                                                        String clientPort) {
        StringBuilder parameters = new StringBuilder();
        parameters.append("-Dnc.runMode=develop\n");
        parameters.append("-Dnc.jstart.server=").append(clientIp).append("\n");
        parameters.append("-Dnc.jstart.port=").append(clientPort).append("\n");
        parameters.append("-Xmx768m\n");
        if (feature < 8) {
            parameters.append("-XX:MaxPermSize=256m\n");
        }
        parameters.append("-Dnc.fi.autogenfile=N\n");
        return parameters.toString();
    }

}
