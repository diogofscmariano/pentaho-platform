package org.pentaho.platform.web.servlet;

import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.server.wadl.generators.resourcedoc.WadlGeneratorResourceDocSupport;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PentahoWadlGeneratorConfig extends WadlGeneratorConfig {
  
  private String getOriginalRequest(){
    JAXRSPluginServlet jaxrsPluginServlet = getJAXRSPluginServlet();
    String originalRequest = "";
    if ( jaxrsPluginServlet != null ) {
      originalRequest = (String) jaxrsPluginServlet.requestThread.get();
    }
    if ( originalRequest == null || originalRequest.isEmpty() ) {
      return "/api/application.wadl"; //global api isn't filled
    }
    return originalRequest;
  }
  
  private WadlGeneratorConfigDescriptionBuilder getBuilderForPluginApi(String plugin){
    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" );
    File file = new File( systemPath, plugin + "/resources/wadlExtension.xml" );
    
    try {
      FileInputStream fileInputStream = new FileInputStream( file );
      return generator( WadlGeneratorResourceDocSupport.class ).prop( "resourceDocStream", fileInputStream );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    return null;
  }
  
  private WadlGeneratorConfigDescriptionBuilder getBuilderForGlobalApi(){
    WadlGeneratorConfigDescriptionBuilder builder = generator( WadlGeneratorResourceDocSupport.class );
    builder.prop( "resourceDocStream", "wadlExtension.xml" );
    return builder;
  }
  
  @Override
  public List<WadlGeneratorDescription> configure() {
    String originalRequest = getOriginalRequest();
    
    Pattern pluginPattern = Pattern.compile( ".*\\/plugin\\/([^/]+)\\/api\\/application.wadl" );
    Pattern globalPattern = Pattern.compile( "\\/api\\/application.wadl" );
    Matcher pluginMatcher = pluginPattern.matcher( originalRequest );
    Matcher globalMatcher = globalPattern.matcher( originalRequest );
    
    WadlGeneratorConfigDescriptionBuilder builder = null;
    
    if ( pluginMatcher.matches() ) { //we have a plugin api request
      String plugin = pluginMatcher.group( 1 );
      builder = getBuilderForPluginApi(plugin);
    } else if ( globalMatcher.matches() ) { //we have the global api request
      builder = getBuilderForGlobalApi();
    }

    if ( builder != null ) {
      return builder.descriptions();
    } else {
      return new ArrayList<WadlGeneratorDescription>();
    }
    
  }

  private JAXRSPluginServlet getJAXRSPluginServlet() {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    
    JAXRSPluginServlet jaxrsPluginServlet;
    
    try {
      jaxrsPluginServlet = (JAXRSPluginServlet) pluginManager.getBean( "api" );
    } catch ( Exception e ) {
      return null;
    }
    return jaxrsPluginServlet;
  }
}
