package com.github.misterreg.mavenplugins.buildgraph;

import org.apache.maven.cli.ExecutionEventLogger;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.Logger;

public class ExtendedExecutionEventLogger extends ExecutionEventLogger {

  private BuildGraphDrawer drawer;
  private MavenSession session;
  private Logger logger2;
  
  public ExtendedExecutionEventLogger(Logger logger, BuildGraphDrawer drawer, MavenSession session) {
    super(logger);
    this.drawer = drawer;
    this.session = session;
  }

  @Override
  public void sessionEnded(ExecutionEvent event) {
    super.sessionEnded(event);
    try {
      drawer.report(session);
    } catch (MojoExecutionException e) {
      logger2.error(e.getMessage(), e);
    }
  }
}
