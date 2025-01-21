package org.basex.query.func.db;

import org.basex.query.util.*;

/**
 * Backup function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class BackupFn extends DbAccess {
  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), true, visitor) && visitAll(visitor, args());
  }
}
