package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Abstract class for repository commands.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Jens Erat
 */
abstract class ARepo extends Command {
  /**
   * Constructor for repository commands.
   * @param perm required permission
   * @param args arguments
   */
  ARepo(final Perm perm, final String... args) {
    super(perm, args);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.REPO); // Repository commands are exclusive
  }
}
