package org.basex.core.cmd;

import org.basex.core.*;

/**
 * Abstract class for database backup.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class ABackup extends Command {
  /**
   * Protected constructor.
   * @param a arguments
   */
  ABackup(final String... a) {
    super(Perm.CREATE, false, a);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.BACKUP); // No parallel backup operations
  }
}
