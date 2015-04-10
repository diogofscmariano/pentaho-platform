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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;

public class PentahoWadlGeneratorConfigTest {

  PentahoWadlGeneratorConfig spyConfig;

  @Before
  public void setUp() {
    spyConfig = spy( new PentahoWadlGeneratorConfig() );
  }

  private void initApplicationContext( String postfixPath ) throws Exception {
    IApplicationContext applicationContext = mock( IApplicationContext.class );
    String path = new java.io.File( "." ).getCanonicalPath();
    when( applicationContext.getSolutionPath( anyString() ) ).thenReturn( path + postfixPath );
    PentahoSystem.setApplicationContext( applicationContext );
  }

  @Test
  public void testNoWadlFile() throws Exception {
    initApplicationContext( "\\test-src-wrongpath" );
    List<WadlGeneratorDescription> result = spyConfig.configure();
    Assert.assertTrue( result.size() == 0 );
  }

  @Test
  public void testWadlExist() throws Exception {
    initApplicationContext( "\\test-src" );
    List<WadlGeneratorDescription> result = spyConfig.configure();
    Assert.assertTrue( result.size() == 1 );
  }

  @Test
  public void testWadlExistPlugin() throws Exception {
    // init plugin
    JAXRSPluginServlet pluginServlet = mock( JAXRSPluginServlet.class );
    pluginServlet.requestThread.set( "http://localhost:8080/pentaho/plugin/data-access/api/application.wadl" );
    IPluginManager pluginManager = mock( IPluginManager.class );
    when( pluginManager.getBean( anyString() ) ).thenReturn( pluginServlet );
    PentahoSystem.registerObject( pluginManager );

    initApplicationContext( "\\test-src" );
    List<WadlGeneratorDescription> result = spyConfig.configure();
    Assert.assertTrue( result.size() == 1 );
  }

}
