package com.sevnis.localstackcucumberjunit5demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.localstack.docker.DockerExe;
import cloud.localstack.docker.LocalstackDocker;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import cloud.localstack.docker.command.RegexStream;
import cucumber.api.cli.Main;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LocalstackDockerExtension.class)
///NOTE: localstack 0.9.1 works with localstack-utils  0.1.15
///NOTE: however we need to retag 0.9.1 to latest as localstack only look for latest docker image
///NOTE: pullNewImage need to be configured to false or it will still try to download the latest image
@LocalstackDockerProperties(pullNewImage = false, services = {"lambda", "dynamodb"})
public class CucumberLocalstackTestRunner {

  @BeforeAll
  public static void setup() throws Exception {
    extendLocalstack();
  }

  ///NOTE: localstack sometimes failed to get the serviceToPortMap due to various reasons: docker issue, wrong config.py
  ///NOTE: this extension fixes the issue
  private static void extendLocalstack() throws Exception {
    Field serviceToPortMapField = LocalstackDocker.INSTANCE.getClass().getDeclaredField("serviceToPortMap");
    serviceToPortMapField.setAccessible(true);
    Map<String, Integer> serviceToPortMap = (Map<String, Integer>) serviceToPortMapField.get(LocalstackDocker.INSTANCE);

    if (serviceToPortMap == null || serviceToPortMap.isEmpty()) {

      Field containerIdField = LocalstackDocker.INSTANCE.getLocalStackContainer().getClass()
          .getDeclaredField("containerId");
      containerIdField.setAccessible(true);
      String containerId = (String) containerIdField.get(LocalstackDocker.INSTANCE.getLocalStackContainer());

      String localStackPortConfig = new DockerExe().execute(Arrays.asList("exec", "-i", containerId, "cat",
          "/opt/code/localstack/.venv/lib/python2.7/site-packages/localstack_client/config.py"));

      Map<String, Integer> ports = new RegexStream(
          Pattern.compile("'(\\w+)'\\Q: '{proto}://{host}:\\E(\\d+)'").matcher(localStackPortConfig)).stream()
          .collect(Collectors.toMap(match -> match.group(1),
              match -> Integer.parseInt(match.group(2))));

      serviceToPortMapField.set(LocalstackDocker.INSTANCE, Collections.unmodifiableMap(ports));
    }
  }

  @Test
  public void cucumberTest() {
    String[] argv = new String[]{"--plugin", "pretty", "--glue", "com.sevnis.localstackcucumberjunit5demo",
        "src/test/resources/cucumber.feature"};
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    byte result = Main.run(argv, contextClassLoader);
    assertTrue(result == 0);
  }
}
