package org.basex.core.cmd;

import org.basex.core.*;

/**
 * Abstract class for option commands.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
abstract class AGet extends Command {
  /**
   * Default constructor.
   * @param a arguments
   */
  AGet(final String... a) {
    super(Perm.NONE, a);
  }

  @Override
  public void databases(final LockResult lr) {
    // No locks needed
  }
}
