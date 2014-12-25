package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.jcr.JcrDatasouceAclHelper;

/**
 * @author Andrey Khayrutdinov
 */
public class JcrMetadataAclHelper extends JcrDatasouceAclHelper {

  private final IDomainFileResolver resolver;

  public JcrMetadataAclHelper( IAclNodeHelper aclNodeHelper, IDomainFileResolver resolver ) {
    super( aclNodeHelper );
    this.resolver = resolver;
  }

  public JcrMetadataAclHelper(IDomainFileResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override protected RepositoryFile getDatasourceFile( String domainId ) {
    return resolver.resolveDomainFileFor( domainId );
  }
}
