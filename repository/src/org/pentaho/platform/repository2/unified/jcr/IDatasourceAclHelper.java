package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

import java.util.EnumSet;

/**
 * @author Andrey Khayrutdinov
 */
public interface IDatasourceAclHelper {

  boolean canAccess( String datasourceName, EnumSet<RepositoryFilePermission> permissions );

  boolean canRead( String datasourceName );

  boolean canWrite( String datasourceName );

  RepositoryFileAcl getAclFor( String datasourceName );

  void setAclFor( String datasourceName, RepositoryFileAcl acl );

  void removeAclFor( String datasourceName );
}
