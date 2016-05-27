package com.github.misterreg.mavenplugins.buildgraph;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.SwingConstants;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;

public class BuildGraphDrawer {

  private String outputDirectory;
  private String pngFileName;
  Set<String> excludedProjectsSet = new HashSet<String>();
  private MaskHelper maskHelper;
  private EGraphOrientation orientation;
  private int pngScale;
  
  public BuildGraphDrawer (
      String outputDirectory, 
      String pngFileName, 
      String[] excludeProjects, 
      String projectMask, 
      String graphOrientation,
      Integer pngScale) throws MojoExecutionException {
    this.outputDirectory = outputDirectory;
    this.pngFileName = pngFileName;
    if (excludeProjects != null && excludeProjects.length > 0) {
      excludedProjectsSet.addAll(Arrays.asList(excludeProjects));
    }
    this.maskHelper = new MaskHelper(projectMask);
    this.orientation = EGraphOrientation.fromHumanName(graphOrientation);
    this.pngScale = pngScale.intValue();
  }
  
  private File getFile() throws IOException, MojoExecutionException {
    File dir = new File (outputDirectory);
    if (dir.exists()) {
      if (!dir.isDirectory()) {
        throw new MojoExecutionException(outputDirectory + " is not directory");
      }
    } else {
      if (!dir.mkdirs()) {
        throw new MojoExecutionException("Unable to create " + dir);
      }
    }
        
    File result = new File(outputDirectory, pngFileName + ".png");
    return result;
  }

  private Logger LOG = Logger.getLogger(BuildGraphDrawer.class.getName());
  
  private Map<String, ProjectBuildInfo> getBuildInfoFromSession (MavenSession session) throws MojoExecutionException {
    Map<String, ProjectBuildInfo> result = new HashMap<String, ProjectBuildInfo>();
    MavenExecutionResult er = session.getResult();
    LOG.info("gathering information projects from reactor:");
    for (MavenProject prj : session.getProjects()) {
      String key = maskHelper.getProjectKey(prj);
      LOG.info("gathering information about project " + key + " from reactor");
      ProjectBuildInfo pbInfo = new ProjectBuildInfo(prj, er);
      result.put(key, pbInfo);
    }
    LOG.info("gathering ended");
    return result;
  }
  
  public void report(MavenSession session) throws MojoExecutionException {
    Map<String, ProjectBuildInfo> buildInfo = getBuildInfoFromSession(session);
    
    LOG.info("Start BuildGraph generation");
    DirectedGraph<String, NonameEdge> g = new DefaultDirectedGraph<String, NonameEdge>(NonameEdge.class);

    //fill vertexes
    for (MavenProject prj : session.getProjects()) {
      String key = maskHelper.getProjectKey(prj);
      if (!excludedProjectsSet.contains(key)) {
        LOG.info("adding vertex to graph " + key);
        g.addVertex(key);
      }
    }
    
    //fill edges after all vertexes are correct
    for (MavenProject prj : session.getProjects()) {
      String prjKey = maskHelper.getProjectKey(prj);
      for (Dependency d : prj.getDependencies()) {
        String depKey = maskHelper.getDependencyKey(d);
        LOG.info("adding dependency to graph " + prjKey + " -> " + depKey);
        if (!g.containsVertex(depKey)) {
          LOG.info("dependency excluded due to vertex " + depKey + " not found");
          continue;
        }
        if (!g.containsVertex(prjKey)) {
          LOG.info("dependency excluded due to vertex " + prjKey + " not found");
          continue;
        }
        g.addEdge(depKey, prjKey);
      }
    }
    
    //generate graph model & image
    JGraphXAdapter<String, NonameEdge> xAdapter = new JGraphXAdapter<String, NonameEdge>(g);
    mxIGraphLayout layout = new mxHierarchicalLayout(xAdapter, orientation.getSwingOrientation());
    layout.execute(xAdapter.getDefaultParent());
    mxGraphComponent gComponent = new mxGraphComponent(xAdapter);
    
    HashMap<String, mxICell> vertexToCellMap = xAdapter.getVertexToCellMap();

    //mark all cells to ensure they are colored correctly
    for (mxICell cell : vertexToCellMap.values()) {
      markCell(xAdapter, cell, EBuildResult.NOT_STARTED);
    }
    
    //not all cells are tracked in sessions
    for (MavenProject prj : session.getProjects()) {
      String key = maskHelper.getProjectKey(prj);
      if (!excludedProjectsSet.contains(key)) {
        mxICell cell = vertexToCellMap.get(key);
        ProjectBuildInfo pbInfo = buildInfo.get(key);
        if (pbInfo != null) {
          markCell(xAdapter, cell, pbInfo.getBuildResult());
        } else {
          LOG.info("no launch information about project " + key);
        }
      }
    }
    
    //file generation
    try {
      writePNGFile(gComponent, getFile());
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    LOG.info("End BuildGraph generation");
  }
  
  private void markCell(
      JGraphXAdapter<String, NonameEdge> xAdapter, 
      mxICell cell, 
      EBuildResult eBuildResult) {
    xAdapter.setCellStyle("fillColor=#" + eBuildResult.getHtmlColor(), new mxICell[]{cell});
  }
  
  private void writePNGFile (mxGraphComponent jg, File f) {
    FileOutputStream outputStream = null;
    try {
      LOG.info("writing png file to " + f.getAbsolutePath());
      BufferedImage image = mxCellRenderer.createBufferedImage(jg.getGraph(), null, pngScale, Color.WHITE, jg.isAntiAlias(), null, jg.getCanvas());
      mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
      //param.setCompressedText(new String[] { "mxGraphModel", erXmlString });

      outputStream = new FileOutputStream(f);
      mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
      if (image != null) {
          encoder.encode(image);
      }
    } catch (IOException e) {
      LOG.info(e.getMessage());
    } finally {
      try {
          outputStream.close();
      } catch (Exception ex) {
      }
    }
    LOG.info("total file size " + f.length() + " bytes");
  }
}
