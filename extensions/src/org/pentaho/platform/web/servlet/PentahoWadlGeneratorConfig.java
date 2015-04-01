package org.pentaho.platform.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.server.wadl.generators.resourcedoc.WadlGeneratorResourceDocSupport;

public class PentahoWadlGeneratorConfig extends WadlGeneratorConfig {

  private static final String WADL_RESOURCE_FILE = "wadlExtension.xml";

  private String getOriginalRequest() {
    JAXRSPluginServlet jaxrsPluginServlet = getJAXRSPluginServlet();
    String originalRequest = "";
    if ( jaxrsPluginServlet != null ) {
      originalRequest = (String) jaxrsPluginServlet.requestThread.get();
    }
    if ( originalRequest == null || originalRequest.isEmpty() ) {
      return "/api/application.wadl"; // global api isn't filled
    }
    return originalRequest;
  }

  private WadlGeneratorConfigDescriptionBuilder getBuilder( String plugin ) {
    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" );

    File file;
    if ( plugin != null ) {
      file = new File( systemPath, plugin + "/resources/" + WADL_RESOURCE_FILE );
    } else {
      file = new File( systemPath, WADL_RESOURCE_FILE );
    }

    try {
      FileInputStream fileInputStream = new FileInputStream( file );
      return generator( WadlGeneratorResourceDocSupport.class ).prop( "resourceDocStream", fileInputStream );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public List<WadlGeneratorDescription> configure() {
    String originalRequest = getOriginalRequest();

    Pattern pluginPattern = Pattern.compile( ".*\\/plugin\\/([^/]+)\\/api\\/application.wadl" );
    Matcher pluginMatcher = pluginPattern.matcher( originalRequest );

    String plugin = null;
    if ( pluginMatcher.matches() ) {
      plugin = pluginMatcher.group( 1 );
    }

    WadlGeneratorConfigDescriptionBuilder builder = getBuilder( plugin );

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
