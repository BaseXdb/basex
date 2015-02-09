package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Exit extends Command {
  /** Constructor. */
  public Exit() {
    super(Perm.NONE);
  }

  @Override
  protected boolean run() {
    return new Close().run(context);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
  }
}
