package reg;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

public class MaskHelper {

  /**
   * supported patterns
   * <li>$groupId</li>
   * <li>$artifactId</li>
   * <li>$vertex</li>
   * @param mask
   */
  
  private String mask;
  
  public MaskHelper (String mask) {
    this.mask = mask;
  }
  
  private String getKey (String groupId, String artifactId, String version) {
    return mask
        .replace("$groupId", groupId)
        .replace("$artifactId", artifactId)
        .replace("$version", version);
  }
  
  public String getDependencyKey(Dependency d) {
    return getKey(d.getGroupId(), d.getArtifactId(), d.getVersion());
  }

  public String getProjectKey (MavenProject prj) {
    return getKey(prj.getGroupId(), prj.getArtifactId(), prj.getVersion());
  }

}
