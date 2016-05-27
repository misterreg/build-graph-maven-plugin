package com.github.misterreg.mavenplugins.buildgraph;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.eventspy.internal.EventSpyDispatcher;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "build-graph")
public class BuildGraphMojo extends AbstractMojo
{
  private Logger LOG = Logger.getLogger(BuildGraphMojo.class.getName());
  @Parameter( defaultValue = "${session}", readonly = true )
  private MavenSession session;
  
  /**
   * Here you can specify target directory for report generation
   */
  @Parameter( defaultValue = "${project.build.directory}"
      )
  private String outputDirectory;

  /**
   * Name of generated png file with graph
   */
  @Parameter( defaultValue = "build-graph"
      )
  private String pngFileName;
  
  /**
   * You can exclude several projects from graph, for example:
   * <code>
   * &lt;excludeProjects&gt;
   *   &lt;param&gt;project1&lt;/param&gt;
   *   &lt;param&gt;project2&lt;/param&gt;
   * &lt;/excludeProjects&gt;
   * </code>
   * 
   * Name should be in mask notation
   */
  @Parameter( 
      )
  private String[] excludeProjects;
  
  /**
   * mask for project name in graph. You can use patterns:
   * <ul>
   * <li>$groupId</li>
   * <li>$artifactId</li>
   * <li>$version</li>
   * </ul>
   */
  @Parameter( 
      defaultValue = "$artifactId"
      )
  private String projectMask;
  
  /** 
   * generated graph orientation. Possible values:
   * <ul>
   * <li>horizontal</li>
   * <li>vertical</li>
   * </ul>
   */
  @Parameter (
      defaultValue = "horizontal"
      )
  private String graphOrientation;
  
  /**
   * PNG image scale
   */
  @Parameter ( defaultValue = "2" )
  private Integer pngScale;
  
  private BuildGraphDrawer drawer;
  
  //untested injection
  //private final static String CLASSNAME_EVENTSPYEL = "org.apache.maven.eventspy.internal.EventSpyExecutionListener";

  private final static String CLASSNAME_EVENTLOGGER = "org.apache.maven.cli.ExecutionEventLogger";
  
  private void launch () throws MojoExecutionException {
    drawer.report(session);
  }
  
  public void execute() throws MojoExecutionException
  {
    drawer = new BuildGraphDrawer(outputDirectory, pngFileName, excludeProjects, projectMask, graphOrientation, pngScale);
    MavenExecutionRequest request = session.getRequest();
    
    ExecutionListener originalListener = request.getExecutionListener();
    String className = originalListener.getClass().getName();
    try {
      
      /*if (CLASSNAME_EVENTSPYEL.equals(className)) {
        //untested
        Field f = originalListener.getClass().getDeclaredField("dispatcher");
        f.setAccessible(true);
        EventSpyDispatcher dispatcher = (EventSpyDispatcher) f.get(originalListener);
        List<EventSpy> spies = dispatcher.getEventSpies();
        spies.add(new EventSpy() {
  
          public void init(Context context) throws Exception {
          }
  
          public void onEvent(Object obj) throws Exception {
            if (obj!= null && obj instanceof ExecutionEvent) {
              ExecutionEvent event = (ExecutionEvent) obj;
              if (ExecutionEvent.Type.SessionEnded.equals(event.getType())) {
                LOG.info("SESSION_ENDED event catched!");
                launch();
              }
            }
          }
  
          public void close() throws Exception {
          }
        });
        dispatcher.setEventSpies(spies);
      } else */ 
      if (CLASSNAME_EVENTLOGGER.equals(className)) {
        Field f = originalListener.getClass().getDeclaredField("logger");
        f.setAccessible(true);
        org.codehaus.plexus.logging.Logger logger = (org.codehaus.plexus.logging.Logger) f.get(originalListener);
        LOG.info("injected SESSION_END event listener");
        request.setExecutionListener(new ExtendedExecutionEventLogger(logger, drawer, session));
      } else {
        LOG.info("unsupported maven event ExecutionListener implementation: " + className);
        launch();
      }
    } catch (NoSuchFieldException e) {
      LOG.info("Exception while injecting " + e.getMessage());
      e.printStackTrace();
      launch();
    } catch (SecurityException e) {
      LOG.info("Exception while injecting " + e.getMessage());
      e.printStackTrace();
      launch();
    } catch (IllegalArgumentException e) {
      LOG.info("Exception while injecting " + e.getMessage());
      e.printStackTrace();
      launch();
    } catch (IllegalAccessException e) {
      LOG.info("Exception while injecting " + e.getMessage());
      e.printStackTrace();
      launch();
    }
  }
}