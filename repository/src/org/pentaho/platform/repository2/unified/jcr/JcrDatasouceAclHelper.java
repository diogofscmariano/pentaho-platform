package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.EnumSet;

/**
 * @author Andrey Khayrutdinov
 */
public abstract class JcrDatasouceAclHelper implements IDatasourceAclHelper {
  protected final IAclNodeHelper aclNodeHelper;

  public JcrDatasouceAclHelper( IAclNodeHelper aclNodeHelper ) {
    if (aclNodeHelper == null) {
      throw new IllegalArgumentException( "AclNodeHelper cannot be null" );
    }
    this.aclNodeHelper = aclNodeHelper;
  }

  public JcrDatasouceAclHelper() {
    this( new JcrAclNodeHelper( PentahoSystem.get( IUnifiedRepository.class ) ) );
  }

  @Override public boolean canRead( String datasourceName ) {
    return canAccess( datasourceName, EnumSet.of( RepositoryFilePermission.READ ) );
  }

  @Override public boolean canWrite( String datasourceName ) {
    return canAccess( datasourceName, EnumSet.of( RepositoryFilePermission.WRITE ) );
  }

  @Override public boolean canAccess( String datasourceName, EnumSet<RepositoryFilePermission> permissions ) {
    RepositoryFile file = getDatasourceFile( datasourceName );
    return ( file != null ) && aclNodeHelper.canAccess( file, permissions );
  }

  @Override public RepositoryFileAcl getAclFor( String datasourceName ) {
    RepositoryFile file = getDatasourceFile( datasourceName );
    return (file == null) ? null : aclNodeHelper.getAclFor( file );
  }

  @Override public void setAclFor( String datasourceName, RepositoryFileAcl acl ) {
    RepositoryFile file = getDatasourceFile( datasourceName );
    if (file != null) {
      aclNodeHelper.setAclFor( file, acl );
    }
  }

  @Override public void removeAclFor( String datasourceName ) {
    setAclFor( datasourceName, null );
  }


  protected abstract RepositoryFile getDatasourceFile(String datasourceName);
}
