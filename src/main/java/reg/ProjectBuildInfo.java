package reg;

import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.BuildSuccess;
import org.apache.maven.execution.BuildSummary;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

public class ProjectBuildInfo {
  // -1 means unknown
  private long duration = -1;
  
  private EBuildResult buildResult = EBuildResult.NOT_STARTED;
  
  public long getDuration() {
    return duration;
  }
  public EBuildResult getBuildResult() {
    return buildResult;
  }
  
  public ProjectBuildInfo(MavenProject prj, MavenExecutionResult er) throws MojoExecutionException {
    BuildSummary sum = er.getBuildSummary(prj);
    if (sum != null) {
      if (sum instanceof BuildSuccess) {
        buildResult = EBuildResult.SUCCESS;
      } else if (sum instanceof BuildFailure) {
        buildResult = EBuildResult.FAILURE;
      } else {
        throw new MojoExecutionException("unknown project result class " + sum.getClass().getName());
      }
      duration = sum.getTime();
    }
  }
}
