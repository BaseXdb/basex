package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Abstract class for database events.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class AEvent extends Command {
  /**
   * Protected constructor.
   * @param args arguments
   */
  AEvent(final String... args) {
    super(Perm.ADMIN, false, args);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.EVENT); // Event operations are exclusive
  }
}
