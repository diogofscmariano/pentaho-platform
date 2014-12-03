package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

/**
 * The interface for operations over shadow nodes.
 *
 * @author Andrey Khayrutdinov
 */
public interface IAclShadowNodeHelper {

  /**
   * Returns <tt>true</tt> if the current user has access to <tt>dataSourceName</tt>.
   * @param dataSourceName  data source
   * @param type            data source's type
   * @return                <tt>true</tt> if the user can access the data source
   */
  boolean hasAccess( String dataSourceName, DatasourceType type );

  /**
   * Returns an ACL rules for <tt>dataSourceName</tt>. If none exists, <tt>null</tt> is returned.
   * @param dataSourceName  data source
   * @param type            data source's type
   * @return                ACL rules if exist or <tt>null</tt> otherwise
   */
  RepositoryFileAcl getAclFor( String dataSourceName, DatasourceType type );

  /**
   * Sets <tt>acl</tt> for <tt>dataSourceName</tt>. If a shadow node does not exist, it is created. If <tt>acl</tt> is
   * <tt>null</tt>, the shadow node is removed.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   * @param acl            an ACL rules for the data source
   */
  void setAclFor( String dataSourceName, DatasourceType type, RepositoryFileAcl acl );

  /**
   * Makes the <tt>dataSourceName</tt> public by removing corresponding shadow node.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   */
  void publishDatasource( String dataSourceName, DatasourceType type );

  /**
   * Returns a path where shadow nodes are created.
   *
   * @return shadow nodes folder's path
   */
  String getShadowFolder();

  enum DatasourceType {
    MONDRIAN {
      @Override String resolveName( String dataSourceName ) {
        return String.format( "%s.mondrian.shadow", dataSourceName );
      }
    },

    METADATA {
      @Override String resolveName( String dataSourceName ) {
        return String.format( "%s.metadata.shadow", dataSourceName );
      }
    };


    abstract String resolveName( String dataSourceName );
  }

}
