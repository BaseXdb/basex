package org.basex.core.cmd;

import org.basex.core.*;

/**
 * Abstract class for repository commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Jens Erat
 */
abstract class ARepo extends Command {
  /**
   * Constructor for repository commands.
   * @param p required permission
   * @param arg arguments
   */
  protected ARepo(final Perm p, final String... arg) {
    super(p, arg);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.REPO); // Repository commands are exclusive
  }
}
