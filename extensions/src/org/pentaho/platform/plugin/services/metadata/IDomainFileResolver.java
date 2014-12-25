package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * @author Andrey Khayrutdinov
 */
interface IDomainFileResolver {
  RepositoryFile resolveDomainFileFor(String domainId);
}
