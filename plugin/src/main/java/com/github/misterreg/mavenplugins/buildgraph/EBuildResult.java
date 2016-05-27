package com.github.misterreg.mavenplugins.buildgraph;

public enum EBuildResult {
  SUCCESS ("DDFFDD"), 
  FAILURE ("FFDDDD"), 
  NOT_STARTED ("FFFFFF");
  
  private String htmlColor;
  private EBuildResult(String htmlColor) {
    this.htmlColor = htmlColor;
  }
  
  public String getHtmlColor () {
    return htmlColor;
  }
}
