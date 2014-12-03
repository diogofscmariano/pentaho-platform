package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;

import java.util.concurrent.Callable;

/**
 * @author Andrey Khayrutdinov
 */
public class JcrAclShadowNodeHelper implements IAclShadowNodeHelper {
  private static final Log logger = LogFactory.getLog( JcrAclShadowNodeHelper.class );

  private final IUnifiedRepository unifiedRepository;
  private final IRepositoryFileAclDao repositoryFileAclDao;
  private final String shadowFolder;

  public JcrAclShadowNodeHelper( IUnifiedRepository unifiedRepository,
                                 IRepositoryFileAclDao repositoryFileAclDao, String shadowFolder ) {
    this.unifiedRepository = unifiedRepository;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.shadowFolder = shadowFolder;
  }

  @Override public boolean hasAccess( final String dataSourceName, final DatasourceType type ) {
    try {
      FindShadowNodeCommand command = new FindShadowNodeCommand( unifiedRepository, getShadowFolder(), type.resolveName( dataSourceName ) );
      boolean nodeExists = SecurityHelper.getInstance().runAsSystem( command );

      return !nodeExists || command.call();
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public RepositoryFileAcl getAclFor( final String dataSourceName, final DatasourceType type ) {
    try {
      GetAclCommand command = new GetAclCommand( unifiedRepository, getShadowFolder(), type.resolveName( dataSourceName ) );
      return SecurityHelper.getInstance().runAsSystem( command );
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public void setAclFor( final String dataSourceName, final DatasourceType type,
                                   final RepositoryFileAcl acl ) {
    try {
      SetAclCommand command =
        new SetAclCommand( unifiedRepository, getShadowFolder(), type.resolveName( dataSourceName ),
          repositoryFileAclDao, acl );
      SecurityHelper.getInstance().runAsSystem( command );
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public void publishDatasource( String dataSourceName, DatasourceType type ) {
    setAclFor( dataSourceName, type, null );
  }

  @Override public String getShadowFolder() {
    return shadowFolder;
  }
}

abstract class AbstractCommand<T> implements Callable<T> {
  final IUnifiedRepository repository;
  final String shadowFolder;
  final String resolvedDsName;

  public AbstractCommand( IUnifiedRepository repository, String shadowFolder, String resolvedDsName ) {
    this.repository = repository;
    this.shadowFolder = shadowFolder;
    this.resolvedDsName = resolvedDsName;
  }

  RepositoryFile getShadowNode() {
    return repository.getFile( RepositoryFilenameUtils.normalize( shadowFolder + "/" + resolvedDsName ) );
  }

  RepositoryFile createShadowNode() {
    RepositoryFile folder = repository.getFile( shadowFolder );
    if ( folder == null ) {
      folder = repository.createFolder(
        repository.getFile( "/" ).getId(),
        new RepositoryFile.Builder( resolvedDsName ).folder( true ).build(),
        ""
      );
    }

    return repository.createFile(
      folder.getId(),
      new RepositoryFile.Builder( resolvedDsName ).build(),
      new SampleRepositoryFileData( "", false, 0 ), ""
    );
  }
}

class FindShadowNodeCommand extends AbstractCommand<Boolean> {
  public FindShadowNodeCommand( IUnifiedRepository repository, String shadowFolder, String resolvedDsName ) {
    super( repository, shadowFolder, resolvedDsName );
  }

  @Override public Boolean call() throws Exception {
    return getShadowNode() != null;
  }
}

class GetAclCommand extends AbstractCommand<RepositoryFileAcl> {
  public GetAclCommand( IUnifiedRepository repository, String shadowFolder, String resolvedDsName ) {
    super( repository, shadowFolder, resolvedDsName );
  }

  @Override public RepositoryFileAcl call() throws Exception {
    RepositoryFile shadowNode = getShadowNode();
    if ( shadowNode == null ) {
      return null;
    }
    return repository.getAcl( shadowNode.getId() );
  }
}

class SetAclCommand extends AbstractCommand<RepositoryFileAcl> {
  final IRepositoryFileAclDao aclDao;
  final RepositoryFileAcl acl;

  public SetAclCommand( IUnifiedRepository repository, String shadowFolder, String resolvedDsName,
                        IRepositoryFileAclDao aclDao, RepositoryFileAcl acl ) {
    super( repository, shadowFolder, resolvedDsName );
    this.aclDao = aclDao;
    this.acl = acl;
  }

  @Override public RepositoryFileAcl call() throws Exception {
    RepositoryFile shadowNode = getShadowNode();

    if ( acl == null ) {
      if ( shadowNode != null ) {
        repository.deleteFile( shadowNode.getId(), true, "Removing the shadow node: " + shadowNode.getPath() );
      }
    } else {
      if ( shadowNode == null ) {
        shadowNode = createShadowNode();
      }
      return aclDao.createAcl( shadowNode.getId(), acl );
    }

    return null;
  }
}