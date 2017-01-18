package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Exit extends Command {
  /** Constructor. */
  public Exit() {
    super(Perm.NONE);
  }

  @Override
  protected boolean run() {
    return close(context);
  }

  @Override
  public void addLocks() {
    job().locks.reads.add(Locking.CONTEXT);
  }
}
