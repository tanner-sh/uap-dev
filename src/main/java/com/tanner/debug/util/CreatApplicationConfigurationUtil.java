package com.tanner.debug.util;

import static com.intellij.execution.ShortenCommandLine.CLASSPATH_FILE;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.tanner.base.BaseUtil;
import com.tanner.base.BusinessException;
import com.tanner.base.ModuleFileUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.ProjectManager;
import com.tanner.script.studio.ui.preference.prop.ClusterInfo;
import com.tanner.script.studio.ui.preference.prop.DomainInfo;
import com.tanner.script.studio.ui.preference.prop.IpAndPort;
import com.tanner.script.studio.ui.preference.prop.SingleServerInfo;
import com.tanner.script.studio.ui.preference.xml.PropXml;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

public class CreatApplicationConfigurationUtil {

  public static final int DEFALUT_PORT = 80;
  public static final String DEFALUT_IP = "127.0.0.1";
  private static final String serverClass = "ufmiddle.start.tomcat.StartDirectServer";
  private static final String clientClass = "nc.starter.test.JStarter";

  /**
   * 设置启动application
   *
   * @param event
   * @param serverFlag
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
    //循环判断当前配置中有没有当前模块的启动配置
    boolean hasFlag = false;
    if (!configurationsList.isEmpty()) {
      for (RunConfiguration configuration : configurationsList) {
        if (configuration.getName().equals(configName)) {
          hasFlag = true;
          ApplicationConfiguration conf = (ApplicationConfiguration) configuration;
          setConfiguration(selectModule, conf, serverFlag);
          break;
        }
      }
    }
    //新增config
    if (!hasFlag) {
      ApplicationConfiguration conf = new ApplicationConfiguration(configName, project,
          ApplicationConfigurationType.getInstance());
      setConfiguration(selectModule, conf, serverFlag);
      RunnerAndConfigurationSettings runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl(
          runManager, conf);
      runManager.addConfiguration(runnerAndConfigurationSettings);
    }
  }

  private static void setConfiguration(Module selectModule, ApplicationConfiguration conf,
      boolean serverFlag) throws BusinessException {
    //检查并设置nc home
    String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
    if (StringUtils.isBlank(homePath)) {
      Messages.showMessageDialog("请先设置NC Home", "Error", Messages.getErrorIcon());
      return;
    }
    PropXml propXml = new PropXml();
    String filename = new File(homePath).getPath() + "/ierp/bin/prop.xml";
    File file = new File(filename);
    if (!file.exists()) {
      Messages.showMessageDialog("file :prop.xml not exists!", "Error", Messages.getErrorIcon());
      return;
    }
    Map<String, String> envs = conf.getEnvs();
    if (serverFlag) {
      conf.setMainClassName(serverClass);
      String exModulesStr = UapProjectEnvironment.getInstance().getEx_modules();
      if (StringUtils.isBlank(exModulesStr)) {
        exModulesStr = "";
        File homeFile = new File(homePath + File.separator + "modules");
        if (homeFile.exists()) {
          File[] modules = homeFile.listFiles();
          if (modules != null) {
            for (File module : modules) {
              String moduleName = module.getName();
              if (ModuleFileUtil.getModuleSet().contains(moduleName)) {
                continue;
              }
              exModulesStr += "," + moduleName;
            }
            if (exModulesStr.length() > 0) {
              exModulesStr = exModulesStr.substring(1);
              UapProjectEnvironment.getInstance().setEx_modules(exModulesStr);
            }
          }
        }
      }
      envs.put("FIELD_EX_MODULES", exModulesStr);
      String hotwebs = envs.get("FIELD_HOTWEBS");
      if (StringUtils.isBlank(hotwebs)) {
        hotwebs = "nccloud,fs";
      }
      envs.put("FIELD_HOTWEBS", hotwebs);
      envs.put("FIELD_ENCODING", "UTF-8");

      String timeZone = envs.get("FIELD_TIMEZONE");
      if (StringUtils.isBlank(timeZone)) {
        timeZone = "GMT+8";
      }
      envs.put("FIELD_TIMEZONE", timeZone);
      conf.setVMParameters("-Dnc.exclude.modules=$FIELD_EX_MODULES$\n" + "-Dnc.runMode=develop\n"
          + "-Dnc.server.location=$FIELD_NC_HOME$\n" + "-DEJBConfigDir=$FIELD_NC_HOME$/ejbXMLs\n"
          + "-DExtServiceConfigDir=$FIELD_NC_HOME$/ejbXMLs\n" + "-Duap.hotwebs=$FIELD_HOTWEBS$\n"
          + "-Duap.disable.codescan=false\n" +
          // 这个参数在最新的home中会影响启动，需要手动添加一个jar包才不会报错
          // "-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl\n" +
          "-Xmx1024m\n" + "-XX:MetaspaceSize=128m\n" + "-XX:MaxMetaspaceSize=512m\n"
          + "-Dorg.owasp.esapi.resources=$FIELD_NC_HOME$/ierp/bin/esapi\n"
          + "-Dfile.encoding=$FIELD_ENCODING$\n" +
          // 默认添加时区
          "-Duser.timezone=$FIELD_TIMEZONE$\n");

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
      envs.put("FIELD_CLINET_IP",
          StringUtils.isBlank(ipAndPort.getAddress()) ? DEFALUT_IP : ipAndPort.getAddress());
      envs.put("FIELD_CLINET_PORT",
          String.valueOf(ipAndPort.getPort() == null ? DEFALUT_PORT : ipAndPort.getPort()));
      conf.setMainClassName(clientClass);
      conf.setVMParameters("-Dnc.runMode=develop\n" + " -Dnc.jstart.server=$FIELD_CLINET_IP$\n"
          + " -Dnc.jstart.port=$FIELD_CLINET_PORT$\n" + " -Xmx768m -XX:MaxPermSize=256m\n"
          + " -Dnc.fi.autogenfile=N");
    }
    envs.put("FIELD_NC_HOME", homePath);
    conf.setModule(selectModule);
    conf.setEnvs(envs);
    conf.setWorkingDirectory(homePath);
    conf.setShortenCommandLine(CLASSPATH_FILE);
  }

  /**
   * 更新application
   *
   * @throws BusinessException
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

}
