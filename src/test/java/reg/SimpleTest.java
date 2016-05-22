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
package reg;

import java.io.File;
import java.util.Arrays;
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
  String[] tests = new String[] {
      "testBasic", 
      "testSuccess", 
      "testExclude", 
      "testMask", 
      "testVertical", 
      "testScale"
      };
  
  private void invoke(InvocationRequest request) throws Exception {
    try {
      InvocationOutputHandler outputHandler = new SystemOutHandler();
      Invoker invoker = new DefaultInvoker();
      invoker.setOutputHandler(outputHandler);
      invoker.setErrorHandler(outputHandler);
      InvocationResult result = invoker.execute(request);
      if (result.getExitCode() != 0) {
        throw new Exception("execution error");
      }
    } catch (MavenInvocationException ex) {
      throw new Exception(ex.getMessage(), ex);
    }
  }
  
  @Test
  public void test1() throws Exception {
    // File pom = this.getTestFile("src/test/resources/test-orch/pom.xml");
    File pom = new File("src/test/resources/test-orch/pom.xml");
    Assert.assertNotNull(pom);
    Assert.assertTrue(pom.exists());
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(pom);
    request.setGoals(Arrays.asList("clean"));
    request.setThreads("4");
    request.setDebug(true);
    invoke(request);
    
    request.setGoals(Arrays.asList("package"));
    for (String test : tests) {
      request.setProfiles(Arrays.asList(test));
      invoke(request);
    }
  }
}
