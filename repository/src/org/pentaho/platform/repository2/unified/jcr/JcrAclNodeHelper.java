package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;

import java.io.ByteArrayInputStream;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import static org.pentaho.platform.repository.RepositoryFilenameUtils.normalize;

/**
 * @author Andrey Khayrutdinov
 */
public class JcrAclNodeHelper implements IAclNodeHelper {
  private static final Log logger = LogFactory.getLog( JcrAclNodeHelper.class );

  private static final String AUTHENTICATED_ROLE = "Authenticated";
  private static final String ACL_SUFFIX = ".acl";

  private final IUnifiedRepository unifiedRepository;
  private final String aclNodeFolder;

  public JcrAclNodeHelper( IUnifiedRepository unifiedRepository,
                           String aclNodeFolder ) {
    this.unifiedRepository = unifiedRepository;
    this.aclNodeFolder = StringUtils.defaultIfEmpty( aclNodeFolder, ServerRepositoryPaths.getAclNodeFolderPath() );
  }

  private String getAclNodePath( String filename ) {
    return normalize( getAclNodeFolder() + RepositoryFile.SEPARATOR + filename );
  }

  private RepositoryFile getAclNode( String filename ) {
    return unifiedRepository.getFile( getAclNodePath( filename ) );
  }

  private RepositoryFile createAclNodeInternal( RepositoryFile folder, String filename ) {
    return unifiedRepository.createFile( folder.getId(), new RepositoryFile.Builder( filename ).aclNode( true ).build(),
      new SimpleRepositoryFileData( new ByteArrayInputStream( new byte[ 0 ] ), "", "" ), "" );
  }

  private RepositoryFile getAclNodeRepositoryFolder() {
    RepositoryFile folder;

    try {
      folder = unifiedRepository.getFile( getAclNodeFolder() );
    } catch ( Exception t ) {
      logger.error( Messages.getInstance().getString( "AclNodeHelper.ERROR_0001_ROOT_FOLDER_NOT_AVAILABLE",
        aclNodeFolder, ServerRepositoryPaths.getAclNodeFolderPath() ) );
      folder = unifiedRepository.getFile( ServerRepositoryPaths.getAclNodeFolderPath() );
    }
    return folder;
  }

  private RepositoryFile createAclNode(String filename ) {
    RepositoryFile folder = getAclNodeRepositoryFolder();

    RepositoryFile aclNode = createAclNodeInternal( folder, filename );
    RepositoryFileAcl aclNodeAcl = new RepositoryFileAcl.Builder( unifiedRepository.getAcl( aclNode.getId() ) )
      .ace( AUTHENTICATED_ROLE, RepositoryFileSid.Type.ROLE, EnumSet.of( RepositoryFilePermission.ALL ) )
      .build();
    unifiedRepository.updateAcl( aclNodeAcl );
    return aclNode;
  }

  @Override public boolean hasAccess( String dataSourceName, DatasourceType type ) {
    String aclNodePath = getAclNodePath( type.resolveName( dataSourceName ) );
    boolean nodeExists = unifiedRepository.hasAccess( aclNodePath, EnumSet.of( RepositoryFilePermission.READ ) );

    return !nodeExists ||
      unifiedRepository.hasAccess( aclNodePath + ACL_SUFFIX, EnumSet.of( RepositoryFilePermission.READ ) );
  }

  @Override public RepositoryFileAcl getAclFor( String dataSourceName, DatasourceType type ) {
    String resolvedDsName = type.resolveName( dataSourceName );
    RepositoryFile aclNode = getAclNode( resolvedDsName );
    if ( aclNode == null ) {
      return null;
    }

    RepositoryFile aclStore = unifiedRepository.getFile( getAclNodePath( resolvedDsName ) + ACL_SUFFIX );
    return ( aclStore == null ) ? null : unifiedRepository.getAcl( aclStore.getId() );
  }

  @Override public void setAclFor( String dataSourceName, DatasourceType type, RepositoryFileAcl acl ) {
    String resolvedName = type.resolveName( dataSourceName );

    RepositoryFile aclNode = getAclNode(resolvedName);

    if ( acl == null ) {
      if ( aclNode != null ) {
        unifiedRepository.deleteFile( aclNode.getId(), true,
          Messages.getInstance().getString( "AclNodeHelper.WARN_0001_REMOVE_ACL_NODE", aclNode.getPath() ) );
      }
    } else {
      if ( aclNode == null ) {
        createAclNode(resolvedName);
      }

      String aclStoreName = resolvedName + ACL_SUFFIX;
      RepositoryFile aclStore = getAclNode( aclStoreName );
      if (aclStore == null) {
        aclStore = createAclNodeInternal( getAclNodeRepositoryFolder(), aclStoreName );
      }
      RepositoryFileAcl existing = unifiedRepository.getAcl( aclStore.getId() );
      RepositoryFileAcl updated = new RepositoryFileAcl.Builder( existing ).aces( acl.getAces() ).build();
      unifiedRepository.updateAcl( updated );
    }
  }

  @Override public void publishDatasource( String dataSourceName, DatasourceType type ) {
    setAclFor( dataSourceName, type, null );
  }

  @Override public void removeAclNodeFor( String dataSourceName, DatasourceType type ) {
    setAclFor( dataSourceName, type, null );
  }

  @Override public String getAclNodeFolder() {
    return aclNodeFolder;
  }
}
