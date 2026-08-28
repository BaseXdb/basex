package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Abstract class for database backup.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ABackup extends Command {
  /**
   * Protected constructor.
   * @param args arguments
   */
  ABackup(final String... args) {
    super(Perm.CREATE, false, args);
  }

  /**
   * Adds a lock for the backups of the database addressed by the specified argument.
   * @param list lock list
   * @param index argument index
   */
  final void addBackupLocks(final LockList list, final int index) {
    final String name = index < args.length && args[index] != null ? args[index] : "";
    final String db = name.matches(".*[?*,].*") ? "" : Databases.name(name);
    if(db.isEmpty()) list.addGlobal();
    else list.add(Locking.backup(db));
  }
}
