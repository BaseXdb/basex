package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Abstract class for database backup.
 *
 * @author BaseX Team 2005-12, BSD License
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

  /**
   * Protected constructor.
   * @param d requires opened database
   * @param a arguments
   */
  ABackup(final boolean d, final String... a) {
    super(Perm.CREATE, d, a);
  }

  @Override
  public boolean databases(final StringList db) {
    db.add(DBLocking.BACKUP);
    return true;
  }
}
