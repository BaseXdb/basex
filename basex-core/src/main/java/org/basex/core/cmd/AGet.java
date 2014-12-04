package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Abstract class for option commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class AGet extends Command {
  /**
   * Default constructor.
   * @param args arguments
   */
  AGet(final String... args) {
    super(Perm.NONE, args);
  }

  @Override
  public void databases(final LockResult lr) {
    // No locks needed
  }
}
