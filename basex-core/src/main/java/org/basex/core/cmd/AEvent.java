package org.basex.core.cmd;

import org.basex.core.*;

/**
 * Abstract class for database events.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
abstract class AEvent extends Command {
  /**
   * Protected constructor.
   * @param a arguments
   */
  AEvent(final String... a) {
    super(Perm.ADMIN, false, a);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.EVENT); // Event operations are exclusive
  }
}
