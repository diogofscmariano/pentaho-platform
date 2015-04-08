package org.pentaho.platform.web.servlet;

import org.pentaho.platform.api.util.IWadlDocumentResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


public class DefaultSpringWadlResourceDocument implements IWadlDocumentResource {
  Resource springResource;
  String pluginId = "";
  boolean isPlugin = false;

  private static String WADL_NAME = "META-INF/wadl/wadlExtension.xml";

  public DefaultSpringWadlResourceDocument( Resource resource ) {
    this.springResource = resource;

    //identify if is plugin
    if ( this.springResource instanceof FileSystemResource ) {
      isPlugin = false;
    } else if ( springResource instanceof ClassPathResource
        && ( (ClassPathResource) springResource ).getClassLoader() instanceof PluginClassLoader ) {
      isPlugin = true;
      pluginId = ( (PluginClassLoader) ( (ClassPathResource) springResource )
          .getClassLoader() ).getPluginDir().getName();
    }
  }

  @Override
  public InputStream getResourceAsStream() throws IOException {
    Enumeration<URL> urls;

    urls = ( (ClassPathResource) springResource ).getClassLoader().getResources( WADL_NAME );

    InputStream is = null;
    URL url = null;

    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" );

    while ( urls.hasMoreElements() ) {
      url = urls.nextElement();
      if ( !isPlugin ) {
        break;
      } else {
        String urlString = url.getPath();
        if ( urlString.contains( systemPath + "/" + pluginId ) ) {
          break;
        }
      }
    }

    if ( url != null ) {
      try {
        is = url.openConnection().getInputStream();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }

    return is;
  }

  @Override
  public boolean isFromPlugin() {
    return isPlugin;
  }

  @Override
  public String getPluginId() {
    return pluginId;
  }
}
