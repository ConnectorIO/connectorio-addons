/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.itests.exam.openhab.container;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.rbc.Constants.RMI_HOST_PROPERTY;
import static org.ops4j.pax.exam.rbc.Constants.RMI_NAME_PROPERTY;
import static org.ops4j.pax.exam.rbc.Constants.RMI_PORT_PROPERTY;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.karaf.container.internal.DependenciesDeployer;
import org.ops4j.pax.exam.karaf.container.internal.InternalKarafDistributionConfigurationOption;
import org.ops4j.pax.exam.karaf.container.internal.JavaVersionUtil;
import org.ops4j.pax.exam.karaf.container.internal.JoinUtil;
import org.ops4j.pax.exam.karaf.container.internal.KarafConfigurationFile;
import org.ops4j.pax.exam.karaf.container.internal.KarafConfigurationFileFactory;
import org.ops4j.pax.exam.karaf.container.internal.KarafTestContainer;
import org.ops4j.pax.exam.karaf.container.internal.adaptions.KarafManipulator;
import org.ops4j.pax.exam.karaf.container.internal.adaptions.KarafManipulatorFactory;
import org.ops4j.pax.exam.karaf.container.internal.runner.Runner;
import org.ops4j.pax.exam.karaf.options.DoNotModifyLogOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionBaseConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationConsoleOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFilePutOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileReplacementOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationSecurityOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.KarafExamSystemConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.karaf.options.KeepRuntimeFolderOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.karaf.options.configs.CustomProperties;
import org.ops4j.pax.exam.karaf.options.configs.FeaturesCfg;
import org.ops4j.pax.exam.karaf.options.libraries.OverrideJUnitBundlesOption;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.PropagateSystemPropertyOption;
import org.ops4j.pax.exam.options.ServerModeOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.extra.EnvironmentOption;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coped and modified version of Karaf Test Container which can survive custom directory layout
 * required by openHAB.
 */
public class OpenHABTestContainer implements TestContainer {

  private static final Logger LOGGER = LoggerFactory.getLogger(KarafTestContainer.class);

  private static final String KARAF_TEST_CONTAINER = "KarafTestContainer.start";
  private static final String EXAM_INJECT_PROPERTY = "pax.exam.inject";
  private static final MavenArtifactUrlReference EXAM_REPO_URL = maven()
      .groupId("org.ops4j.pax.exam").artifactId("pax-exam-features")
      .version(Info.getPaxExamVersion()).type("xml");

  private final Runner runner;
  private final ExamSystem system;
  private KarafDistributionBaseConfigurationOption framework;
  @SuppressWarnings("unused")
  private KarafManipulator versionAdaptions;
  private boolean started;
  private RBCRemoteTarget target;

  private File targetFolder;
  private File karafBase;

  private Registry registry;

  private static boolean isJava9Compatible;

  static {
    setJava9Compatible(JavaVersionUtil.getMajorVersion() >= 9);
  }

  public OpenHABTestContainer(ExamSystem system, KarafDistributionBaseConfigurationOption framework, Runner runner) {
    this.framework = framework;
    this.system = system;
    this.runner = runner;
  }

  @Override
  public synchronized TestContainer start() {
    try {
      String name = system.createID(KARAF_TEST_CONTAINER);

      Option invokerConfiguration = getInvokerConfiguration();

      //registry.selectGracefully();
      String host = InetAddress.getLoopbackAddress().getHostAddress();
      System.setProperty("java.rmi.server.hostname", host);
      int port = openRegistryOnFreePort(host, 21000, 21099);

      ExamSystem subsystem = system.fork(options(
          systemProperty("java.rmi.server.hostname").value(host),
          systemProperty(RMI_HOST_PROPERTY).value(host),
          systemProperty(RMI_PORT_PROPERTY).value(Integer.toString(port)),
          systemProperty(RMI_NAME_PROPERTY).value(name),
          invokerConfiguration,
          systemProperty(EXAM_INJECT_PROPERTY).value("true"),
          editConfigurationFileExtend("etc/system.properties", "jline.shutdownhook", "true")
      ));
      target = new RBCRemoteTarget(name, port, subsystem.getTimeout());

      System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");

      if (framework.getExisting() != null) {
        targetFolder = framework.getExisting();
      } else {
        URL sourceDistribution = new URL(framework.getFrameworkURL());
        targetFolder = retrieveFinalTargetFolder(subsystem);
        ArchiveExtractor.extract(sourceDistribution, targetFolder);
      }

      karafBase = searchKarafBase(targetFolder);
      File karafHome = karafBase;

      versionAdaptions = createVersionAdapter(karafBase);
      DependenciesDeployer deployer = new DependenciesDeployer(subsystem, karafBase, karafHome);
      deployer.copyBootClasspathLibraries();

      if (framework.getExisting() != null) {
        backupConfigFiles();
      }
      setupSystemProperties(karafHome, subsystem);
      updateLogProperties(karafHome, subsystem);

      List<KarafDistributionConfigurationFileOption> options = new ArrayList<>(Arrays.asList(
          subsystem.getOptions(KarafDistributionConfigurationFileOption.class)
      ));
      options.addAll(fromFeatureOptions(subsystem.getOptions(KarafFeaturesOption.class)));
      String usedExamFeature = shouldInjectJUnitBundles(system) ? "exam" : "exam-no-junit";
      options.addAll(fromFeatureOptions(KarafDistributionOption.features(EXAM_REPO_URL, usedExamFeature)));

      if (framework.isUseDeployFolder()) {
        deployer.copyReferencedArtifactsToDeployFolder();
      }
      else {
        options.addAll(fromFeatureOptions(deployer.getDependenciesFeature()));
      }

      options.addAll(configureBootDelegation(subsystem));
      options.addAll(configureSystemPackages(subsystem));
      updateUserSetProperties(karafHome, options);

      startKaraf(subsystem, karafBase, karafHome);
      started = true;
    }
    catch (IOException e) {
      throw new RuntimeException("Problem starting container", e);
    }
    return this;
  }

  private void backupConfigFiles() {
    try {
      File karafEtc = new File(karafBase, framework.getKarafEtc());
      FileUtils.copyFile(new File(karafEtc, "config.properties"), new File(karafEtc, "config.properties.paxexam"));
      FileUtils.copyFile(new File(karafEtc, "system.properties"), new File(karafEtc, "system.properties.paxexam"));
      FileUtils.copyFile(new File(karafEtc, "org.apache.karaf.features.cfg"), new File(karafEtc, "org.apache.karaf.features.cfg.paxexam"));
      FileUtils.copyFile(new File(karafEtc, "org.ops4j.pax.logging.cfg"), new File(karafEtc, "org.ops4j.pax.logging.cfg.paxexam"));
    } catch (Exception e) {
      LOGGER.warn("Can't backup config files", e);
    }
  }

  private void restoreConfigFiles() {
    try {
      File karafEtc = new File(karafBase, framework.getKarafEtc());
      FileUtils.copyFile(new File(karafEtc, "config.properties.paxexam"), new File(karafEtc, "config.properties"));
      FileUtils.copyFile(new File(karafEtc, "system.properties.paxexam"), new File(karafEtc, "system.properties"));
      FileUtils.copyFile(new File(karafEtc, "org.apache.karaf.features.cfg.paxexam"), new File(karafEtc, "org.apache.karaf.features.cfg"));
      FileUtils.copyFile(new File(karafEtc, "org.ops4j.pax.logging.cfg.paxexam"), new File(karafEtc, "org.ops4j.pax.logging.cfg"));
    } catch (Exception e) {
      LOGGER.warn("Can't restore config files", e);
    }
  }

  private int openRegistryOnFreePort(String host, int minPort, int maxPort) throws RemoteException {
    for (int port = minPort; port <= maxPort; port++) {
      try {
        LOGGER.trace("Creating RMI registry server on {}:{}", host, port);
        registry = LocateRegistry.createRegistry(port);
        LOGGER.info("Created RMI registry server on {}:{}", host, port);
        return port;
      } catch (RemoteException ex) {
        if (ex.detail instanceof BindException) {
          LOGGER.trace("Tried to open RMI registry on {}: {} but failed.", host, port, ex);
          if (port >= maxPort) {
            throw ex;
          }
        } else {
          throw ex;
        }
      }
    }
    throw new IllegalStateException("Could not open RMI registry");
  }

  private boolean shouldInjectJUnitBundles(ExamSystem _system) {
    Option[] options = _system.getOptions(OverrideJUnitBundlesOption.class);
    LOGGER.info("Found {} options when requesting OverrideJUnitBundlesOption.class", options.length);
    return options.length == 0;
  }

  private KarafManipulator createVersionAdapter(File karafBase) {
    File karafEtc = new File(karafBase, framework.getKarafEtc());
    File distributionInfo = new File(karafEtc, "distribution.info");

    framework = new InternalKarafDistributionConfigurationOption(framework, distributionInfo);
    return KarafManipulatorFactory.createManipulator(framework.getKarafVersion());
  }

  private void startKaraf(ExamSystem subsystem, File karafBase, File karafHome) {
    long startedAt = System.currentTimeMillis();
    File karafBin = new File(karafBase, "bin");
    File karafEtc = new File(karafBase, framework.getKarafEtc());
    File karafData = new File(karafBase, framework.getKarafData());
    File karafLog = new File(karafData, framework.getKarafLog());
    String[] classPath = buildKarafClasspath(karafHome);
    makeScriptsInBinExec(karafBin);
    File javaHome = new File(System.getProperty("java.home"));
    String main = framework.getKarafMain();
    String options = "";
    List<String> environment = new ArrayList<>();
    EnvironmentOption[] environmentOptions = subsystem.getOptions(EnvironmentOption.class);
    for (EnvironmentOption environmentOption : environmentOptions) {
      environment.add(environmentOption.getEnvironment());
    }
    ArrayList<String> javaOpts = new ArrayList<>();
    appendVmSettingsFromSystem(javaOpts, subsystem);
    String[] javaEndorsedDirs = null;
    if (isJava9Compatible()) {
      javaEndorsedDirs = new String[] {};
    }
    else {
      javaEndorsedDirs = new String[] { javaHome + "/jre/lib/endorsed", javaHome + "/lib/endorsed", karafHome + "/lib/endorsed" };
    }
    String[] javaExtDirs = new String[] { javaHome + "/jre/lib/ext", javaHome + "/lib/ext", javaHome + "/lib/ext" };
    List<String> opts = Arrays.asList(
        "-Dkaraf.startLocalConsole=" + shouldLocalConsoleBeStarted(subsystem),
        "-Dkaraf.startRemoteShell=" + shouldRemoteShellBeStarted(subsystem)
    );
    boolean enableMBeanServerBuilder = shouldMBeanServerBuilderBeEnabled(subsystem);
    String[] karafOpts = new String[] {};
    String[] env = environment.toArray(new String[environment.size()]);
    runner.exec(env, karafBase, javaHome.toString(), javaOpts.toArray(new String[] {}),
        javaEndorsedDirs, javaExtDirs, karafHome.toString(), karafData.toString(), karafEtc.toString(), karafLog.toString(),
        karafOpts, opts.toArray(new String[] {}), classPath, main, options,
        enableMBeanServerBuilder);

    LOGGER.debug("Test Container started in {} millis", System.currentTimeMillis() - startedAt);
    LOGGER.info("Wait for test container to finish its initialization {}", subsystem.getTimeout());

    if (subsystem.getOptions(ServerModeOption.class).length == 0) {
      waitForState(org.ops4j.pax.exam.karaf.container.internal.Constants.SYSTEM_BUNDLE, Bundle.ACTIVE, subsystem.getTimeout());
    }
    else {
      LOGGER.info("System runs in Server Mode. Which means, no Test facility bundles available on target system.");
    }
  }

  private boolean shouldDeleteRuntime() {
    boolean deleteRuntime = true;
    if (framework.getExisting() != null) {
      return false;
    }
    KeepRuntimeFolderOption[] keepRuntimeFolder = system.getOptions(KeepRuntimeFolderOption.class);
    if (keepRuntimeFolder != null && keepRuntimeFolder.length != 0) {
      deleteRuntime = false;
    }
    return deleteRuntime;
  }

  private Option getInvokerConfiguration() {
    KarafExamSystemConfigurationOption[] internalConfigurationOptions = system.getOptions(KarafExamSystemConfigurationOption.class);
    Option invokerConfiguration = systemProperty("pax.exam.invoker").value("junit");
    if (internalConfigurationOptions != null && internalConfigurationOptions.length != 0) {
      invokerConfiguration = systemProperty("pax.exam.invoker").value(internalConfigurationOptions[0].getInvoker());
    }
    return invokerConfiguration;
  }

  private String shouldRemoteShellBeStarted(ExamSystem subsystem) {
    KarafDistributionConfigurationConsoleOption[] consoleOptions = subsystem.getOptions(KarafDistributionConfigurationConsoleOption.class);
    if (consoleOptions == null) {
      return "true";
    }
    for (KarafDistributionConfigurationConsoleOption consoleOption : consoleOptions) {
      if (consoleOption.getStartRemoteShell() != null) {
        return consoleOption.getStartRemoteShell() ? "true" : "false";
      }
    }
    return "true";
  }

  private String shouldLocalConsoleBeStarted(ExamSystem subsystem) {
    KarafDistributionConfigurationConsoleOption[] consoleOptions = subsystem.getOptions(KarafDistributionConfigurationConsoleOption.class);
    if (consoleOptions == null) {
      return "true";
    }
    for (KarafDistributionConfigurationConsoleOption consoleOption : consoleOptions) {
      if (consoleOption.getStartLocalConsole() != null) {
        return consoleOption.getStartLocalConsole() ? "true" : "false";
      }
    }
    return "true";
  }

  private boolean shouldMBeanServerBuilderBeEnabled(ExamSystem subsystem) {
    KarafDistributionConfigurationSecurityOption[] securityOptions = subsystem.getOptions(KarafDistributionConfigurationSecurityOption.class);
    if (securityOptions == null) {
      return false;
    }
    for (KarafDistributionConfigurationSecurityOption securityOption : securityOptions) {
      if (securityOption.getEnableKarafMBeanServerBuilder() != null) {
        return securityOption.getEnableKarafMBeanServerBuilder();
      }
    }
    return false;
  }

  private void makeScriptsInBinExec(File karafBin) {
    if (!karafBin.exists()) {
      return;
    }
    File[] files = karafBin.listFiles();
    for (File file : files) {
      file.setExecutable(true);
    }
  }

  private File retrieveFinalTargetFolder(ExamSystem subsystem) {
    if (framework.getUnpackDirectory() == null) {
      return subsystem.getConfigFolder();
    } else {
      File targetDir;
      if (framework.getDirectoryNameFormat() != null) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(framework.getDirectoryNameFormat());
        targetDir = new File(framework.getUnpackDirectory(), simpleDateFormat.format(new Date()));
      } else {
        targetDir = new File(framework.getUnpackDirectory(), UUID.randomUUID().toString());
      }
      targetDir = transformToAbsolutePath(targetDir);
      targetDir.mkdirs();
      return targetDir;
    }
  }

  private File transformToAbsolutePath(File file) {
    return new File(file.getAbsolutePath());
  }

  private void appendVmSettingsFromSystem(ArrayList<String> opts, ExamSystem subsystem) {
    VMOption[] options = subsystem.getOptions(VMOption.class);
    for (VMOption option : options) {
      opts.add(option.getOption());
    }
  }

  private void updateUserSetProperties(File karafHome, List<KarafDistributionConfigurationFileOption> options) throws IOException {
    HashMap<String, HashMap<String, List<KarafDistributionConfigurationFileOption>>> optionMap = new HashMap<>();
    for (KarafDistributionConfigurationFileOption option : options) {
      if (!optionMap.containsKey(option.getConfigurationFilePath())) {
        optionMap.put(option.getConfigurationFilePath(), new HashMap<>());
      }
      Map<String, List<KarafDistributionConfigurationFileOption>> optionEntries = optionMap.get(option.getConfigurationFilePath());
      if (!optionEntries.containsKey(option.getKey())) {
        optionEntries.put(option.getKey(), new ArrayList<>());
      }
      else {
        // if special file warn, replace and continue
        if (!option.getConfigurationFilePath().equals(FeaturesCfg.FILE_PATH)) {
          LOGGER.warn("you're trying to add an additional value to a config file; you're current value will be replaced.");
          optionEntries.put(option.getKey(), new ArrayList<>());
        }
      }
      optionEntries.get(option.getKey()).add(option);
    }
    String karafData = framework.getKarafData();
    String karafLog = framework.getKarafLog();
    String karafEtc = framework.getKarafEtc();
    Set<String> configFiles = optionMap.keySet();
    for (String configFile : configFiles) {
      KarafConfigurationFile karafConfigurationFile = KarafConfigurationFileFactory.create(karafHome, configFile);
      if (configFile.startsWith("data/log/") && !configFile.startsWith(karafData)) {
        karafConfigurationFile = KarafConfigurationFileFactory.create(karafHome, karafLog + configFile.substring(8));
      } else if (configFile.startsWith("data/") && !configFile.startsWith(karafData)) {
        karafConfigurationFile = KarafConfigurationFileFactory.create(karafHome, karafData + configFile.substring(4));
      } else if (configFile.startsWith("etc/") && !configFile.startsWith(karafEtc)) {
        karafConfigurationFile = KarafConfigurationFileFactory.create(karafHome, karafEtc + configFile.substring(3));
      }
      karafConfigurationFile.load();

      Collection<List<KarafDistributionConfigurationFileOption>> optionsToApply = optionMap
          .get(configFile).values();
      boolean store = true;
      for (List<KarafDistributionConfigurationFileOption> optionListToApply : optionsToApply) {
        for (KarafDistributionConfigurationFileOption optionToApply : optionListToApply) {
          if (optionToApply instanceof KarafDistributionConfigurationFilePutOption) {
            karafConfigurationFile.put(optionToApply.getKey(), optionToApply.getValue());
          }
          else if (optionToApply instanceof KarafDistributionConfigurationFileReplacementOption) {
            karafConfigurationFile
                .replace(((KarafDistributionConfigurationFileReplacementOption) optionToApply)
                    .getSource());
            store = false;
            break;
          }
          else {
            karafConfigurationFile
                .extend(optionToApply.getKey(), optionToApply.getValue());
          }
        }
        if (!store) {
          break;
        }
      }
      if (store) {
        karafConfigurationFile.store();
      }
    }
  }

  private Collection<? extends KarafDistributionConfigurationFileOption> configureSystemPackages(ExamSystem subsystem) {
    String systemPackages = JoinUtil.join(subsystem.getOptions(SystemPackageOption.class));
    if (systemPackages.length() == 0) {
      return Arrays.asList();
    }
    return Arrays.asList(new KarafDistributionConfigurationFileExtendOption(CustomProperties.SYSTEM_PACKAGES_EXTRA, systemPackages));
  }

  private Collection<? extends KarafDistributionConfigurationFileOption> configureBootDelegation(ExamSystem subsystem) {
    BootDelegationOption[] bootDelegationOptions = subsystem.getOptions(BootDelegationOption.class);
    return Arrays.asList(new KarafDistributionConfigurationFileExtendOption(CustomProperties.BOOTDELEGATION, JoinUtil.join(bootDelegationOptions)));
  }

  private Collection<? extends KarafDistributionConfigurationFileOption> fromFeatureOptions(KarafFeaturesOption... featuresOptions) {
    ArrayList<KarafDistributionConfigurationFileOption> retVal = new ArrayList<>();

    for (KarafFeaturesOption featuresOption : featuresOptions) {
      retVal.add(new KarafDistributionConfigurationFileExtendOption(FeaturesCfg.REPOSITORIES, featuresOption.getURL()));
      retVal.add(new KarafDistributionConfigurationFileExtendOption(FeaturesCfg.BOOT, JoinUtil.join(featuresOption.getFeatures())));
    }
    return retVal;
  }

  private void setupSystemProperties(File karafHome, ExamSystem _system) throws IOException {
    File customPropertiesFile = new File(karafHome, framework.getKarafEtc() + "/system.properties");
    SystemPropertyOption[] customProps = _system.getOptions(SystemPropertyOption.class);
    Properties karafPropertyFile = new Properties();
    try (final FileInputStream customPropertiesInStream = new FileInputStream(customPropertiesFile)) {
      karafPropertyFile.load(customPropertiesInStream);
      for (SystemPropertyOption systemPropertyOption : customProps) {
        karafPropertyFile.put(systemPropertyOption.getKey(), systemPropertyOption.getValue());
      }
      for (PropagateSystemPropertyOption option : system.getOptions(PropagateSystemPropertyOption.class)) {
        String key = option.getKey();
        String value = System.getProperty(key);
        if (value != null) {
          karafPropertyFile.put(key, value);
        }
      }
      try (final FileOutputStream customPropertiesOutStream = new FileOutputStream(customPropertiesFile)) {
        karafPropertyFile.store(customPropertiesOutStream, "updated by pax-exam");
      }
    }
  }

  private void updateLogProperties(File karafHome, ExamSystem _system) throws IOException {
    DoNotModifyLogOption[] modifyLog = _system.getOptions(DoNotModifyLogOption.class);
    if (modifyLog != null && modifyLog.length != 0) {
      LOGGER.info("Log file should not be modified by the test framework");
      return;
    }

//    LoggingBackend loggingBackend = getLoggingBackend(karafHome);
    String realLogLevel = retrieveRealLogLevel(_system);

    File customPropertiesFile = new File(karafHome, framework.getKarafEtc() + "/org.ops4j.pax.logging.cfg");
    Properties karafPropertyFile = new Properties();
    try (final FileInputStream customPropertiesInStream = new FileInputStream(customPropertiesFile)) {
      karafPropertyFile.load(customPropertiesInStream);
      //loggingBackend.updatePaxLoggingConfiguration(karafPropertyFile, realLogLevel);
      try (final FileOutputStream customPropertiesOutStream = new FileOutputStream(customPropertiesFile)) {
        karafPropertyFile.store(customPropertiesOutStream, "updated by pax-exam");
      }
    }
  }

//  private LoggingBackend getLoggingBackend(File karafHome)
//      throws IOException, FileNotFoundException {
//    File customisedSystemPropertiesFile = new File(karafHome,
//        framework.getKarafEtc() + "/startup.properties");
//    InputStream customisedSystemPropertiesInputStream = null;
//    try {
//      customisedSystemPropertiesInputStream = new FileInputStream(
//          customisedSystemPropertiesFile);
//      Properties customisedSystemProperties = new Properties();
//      customisedSystemProperties.load(customisedSystemPropertiesInputStream);
//
//      Set<Object> systemPropertyNames = customisedSystemProperties.keySet();
//      for (Object systemPropertyName : systemPropertyNames) {
//        if (systemPropertyName.toString().contains("pax-logging-log4j2")) {
//          return LoggingBackend.LOG4J2;
//        }
//      }
//
//      return LoggingBackend.LOG4J;
//
//    }
//    finally {
//      if (customisedSystemPropertiesInputStream != null) {
//        customisedSystemPropertiesInputStream.close();
//      }
//    }
//  }

  private String retrieveRealLogLevel(ExamSystem _system) {
    LogLevelOption[] logLevelOptions = _system.getOptions(LogLevelOption.class);
    return logLevelOptions != null && logLevelOptions.length != 0 ? logLevelOptions[0]
        .getLogLevel().toString() : "WARN";
  }

  private String[] buildKarafClasspath(File karafHome) {
    List<String> cp = new ArrayList<>();
    File[] jars = new File(karafHome + "/lib").listFiles((FileFilter) new WildcardFileFilter(
        "*.jar"));
    for (File jar : jars) {
      cp.add(jar.toString());
    }
    // do the same for lib/boot
    File[] bootJars = new File(karafHome + "/lib/boot")
        .listFiles((FileFilter) new WildcardFileFilter("*.jar"));
    if (bootJars != null) {
      for (File jar : bootJars) {
        cp.add(jar.toString());
      }
    }
    // do the same for lib/ext
    File[] extJars = new File(karafHome + "/lib/ext")
        .listFiles((FileFilter) new WildcardFileFilter("*.jar"));
    if (extJars != null) {
      for (File jar : extJars) {
        cp.add(jar.toString());
      }
    }

    File [] jdk9Plus = new File(karafHome + "/lib/jdk9plus")
        .listFiles((FileFilter) new WildcardFileFilter("*.jar"));

    if (jdk9Plus != null) {
      for (File jar : jdk9Plus) {
        cp.add(jar.toString());
      }
    }

    return cp.toArray(new String[] {});
  }

  /**
   * Since we might get quite deep use a simple breath first search algorithm
   */
  private File searchKarafBase(File _targetFolder) {
    Queue<File> searchNext = new LinkedList<>();
    searchNext.add(_targetFolder);
    while (!searchNext.isEmpty()) {
      File head = searchNext.poll();
      if (!head.isDirectory()) {
        continue;
      }
      boolean isSystem = false;
      boolean etc = false;
      for (File file : head.listFiles()) {
        if (file.isDirectory() && file.getName().equals("system")) {
          isSystem = true;
        }
        if (file.isDirectory() && file.getName().equals("etc")) {
          etc = true;
        }
      }
      if (isSystem && etc) {
        return head;
      }
      searchNext.addAll(Arrays.asList(head.listFiles()));
    }
    throw new IllegalStateException("No karaf base dir found in extracted distribution.");
  }

  @Override
  public synchronized TestContainer stop() {
    LOGGER.debug("Shutting down the test container (Pax Runner)");
    try {
      if (started) {
        target.stop();
        RemoteBundleContextClient remoteBundleContextClient = target.getClientRBC();
        if (remoteBundleContextClient != null) {
          remoteBundleContextClient.stop();

        }
        if (runner != null) {
          runner.shutdown();
        }
        try {
          UnicastRemoteObject.unexportObject(registry, true);
          /*
           * NOTE: javaRunner.waitForExit() works for Equinox and Felix, but not for Knopflerfish,
           * need to investigate why. OTOH, it may be better to kill the process as we're doing
           * now, just to be on the safe side.
           */
        }
        catch (NoSuchObjectException exc) {
          throw new TestContainerException(exc);
        }

      }
      else {
        throw new RuntimeException("Container never came up");
      }
    }
    finally {
      started = false;
      target = null;
      if (framework.getExisting() != null) {
        restoreConfigFiles();
      }
      if (shouldDeleteRuntime()) {
        system.clear();
        try {
          FileUtils.forceDelete(targetFolder);
        }
        catch (IOException e) {
          forceCleanup();
        }
      }
    }
    return this;
  }

  private void forceCleanup() {
    LOGGER.info("Can't remove runtime system; schedule it for exit of the jvm.");
    try {
      FileUtils.forceDeleteOnExit(targetFolder);
    }
    catch (IOException e1) {
      LOGGER.error("Well, this should simply not happen...");
    }
  }

  private void waitForState(final long bundleId, final int state, final RelativeTimeout timeout) {
    target.getClientRBC().waitForState(bundleId, state, timeout);
  }


  @Override
  public synchronized void call(TestAddress address) {
    target.call(address);
  }

  @Override
  public synchronized long install(InputStream stream) {
    return install("local", stream);
  }

  @Override
  public synchronized long install(String location, InputStream stream) {
    return target.install(location, stream);
  }

  @Override
  public String toString() {
    if (framework.getExisting() != null) {
      return "KarafTestContainer{" + framework.getExisting().getAbsolutePath() + "}";
    } else {
      return "KarafTestContainer{" + framework.getFrameworkURL() + "}";
    }
  }

  @Override
  public long installProbe(InputStream stream) {
    return target.installProbe(stream);
  }

  @Override
  public void uninstallProbe() {
    target.uninstallProbe();
  }

  public static boolean isJava9Compatible() {
    return isJava9Compatible;
  }

  private static void setJava9Compatible(boolean java9Compatible) {
    isJava9Compatible = java9Compatible;
  }

}
