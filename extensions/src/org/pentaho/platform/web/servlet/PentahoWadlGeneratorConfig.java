/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.util.IWadlDocumentResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.server.wadl.generators.resourcedoc.WadlGeneratorResourceDocSupport;

public class PentahoWadlGeneratorConfig extends WadlGeneratorConfig {

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
    List<IWadlDocumentResource> resourceReferences = PentahoSystem.getAll( IWadlDocumentResource.class );

    InputStream is = null;
    try {
      for ( IWadlDocumentResource wadlDocumentResource : resourceReferences ) {
        if ( plugin == null && !wadlDocumentResource.isFromPlugin() ) {
          is = wadlDocumentResource.getResourceAsStream();
          break;
        } else if ( wadlDocumentResource.isFromPlugin() && wadlDocumentResource.getPluginId().equals( plugin ) ) {
          is = wadlDocumentResource.getResourceAsStream();
          break;
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    if ( is != null ) {
      return generator( WadlGeneratorResourceDocSupport.class ).prop( "resourceDocStream", is );
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
