package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrDatasouceAclHelper;

/**
 * @author Andrey Khayrutdinov
 */
public class JcrMondrianAclHelper extends JcrDatasouceAclHelper {

  private final MondrianCatalogRepositoryHelper catalogRepositoryHelper;

  // should be replaced with injecting into constructor
  private static MondrianCatalogRepositoryHelper createCatalogRepositoryHelper() {
    return new MondrianCatalogRepositoryHelper( PentahoSystem.get( IUnifiedRepository.class ) );
  }

  public JcrMondrianAclHelper( IAclNodeHelper aclNodeHelper ) {
    super( aclNodeHelper );
    this.catalogRepositoryHelper = createCatalogRepositoryHelper();
  }

  public JcrMondrianAclHelper() {
    super();
    this.catalogRepositoryHelper = createCatalogRepositoryHelper();
  }

  @Override protected RepositoryFile getDatasourceFile( String catalogName ) {
    return catalogRepositoryHelper.getMondrianCatalogFile( catalogName );
  }
}
