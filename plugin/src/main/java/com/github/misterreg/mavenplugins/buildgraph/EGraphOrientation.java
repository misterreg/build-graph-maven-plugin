package com.github.misterreg.mavenplugins.buildgraph;

import javax.swing.SwingConstants;

import org.apache.maven.plugin.MojoExecutionException;

public enum EGraphOrientation {
  HORIZONTAL("horizontal"),
  VERTICAL("vertical");
  private String humanName;
  
  private EGraphOrientation(String humanName) {
    this.humanName = humanName;
  }
  
  public static EGraphOrientation fromHumanName (String humanName) throws MojoExecutionException {
    for (EGraphOrientation orientation : EGraphOrientation.values()) {
      if (humanName.equals(orientation.humanName)) {
        return orientation;
      }
    }
    throw new MojoExecutionException("unknown orientation name \"" + humanName + "\"");
  }
  public int getSwingOrientation () throws MojoExecutionException {
    if (this.equals(HORIZONTAL)) {
      return SwingConstants.WEST;
    } else if (this.equals(VERTICAL)) {
      return SwingConstants.NORTH;
    } else {
      //unknown orientation
      throw new MojoExecutionException("unknown orientation enum " + this);
    }
  }
}

