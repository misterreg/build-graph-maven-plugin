/*
 * Copyright 2016 reg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.misterreg.mavenplugins.buildgraph.tests;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reg
 */

public class SimpleTest {
  Logger LOG = Logger.getLogger(SimpleTest.class.getName());
  String[] tests = new String[] {
      "testBasic", 
      "testSuccess", 
      "testExclude", 
      "testMask", 
      "testVertical", 
      "testScale"
      };
  
  private void invoke(InvocationRequest request, File dir) {
    try {
      InvocationOutputHandler outputHandler = new SystemOutHandler();
      Invoker invoker = new DefaultInvoker();
      invoker.setOutputHandler(outputHandler);
      invoker.setErrorHandler(outputHandler);
      invoker.setWorkingDirectory(dir);
      InvocationResult result = invoker.execute(request);
      if (result.getExitCode() != 0) {
        LOG.info("mvn execution error");
      }
    } catch (MavenInvocationException ex) {
      LOG.info("exception " + ex.getMessage());
      ex.printStackTrace();
    }
  }
  
  @Test
  public void test1() throws Exception {
    File dir = new File("target/test-classes/test-orch");
    File pom = new File(dir, "pom.xml");
    Assert.assertNotNull(pom);
    Assert.assertTrue(pom.exists());
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(pom);
    request.setGoals(Arrays.asList("clean"));
    request.setThreads("4");
    request.setDebug(false);
    invoke(request, dir);
    
    for (String test : tests) {
      request.setPomFile(pom);
      request.setGoals(Arrays.asList("clean"));
      request.setThreads("4");
      request.setDebug(true);
      request.setBaseDirectory(dir);

      request.setGoals(Arrays.asList("package"));
      request.setProfiles(Arrays.asList(test));
      invoke(request, dir);
    }
    LOG.info("Look at " + dir.getAbsolutePath());
    for (String test : tests) {
      LOG.info("check " + test + ".png");
    }    
    LOG.info("Bye!");
  }
}
