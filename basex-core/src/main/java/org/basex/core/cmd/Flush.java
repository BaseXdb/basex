package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;

/**
 * Evaluates the 'flush' command and flushes the database buffers.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Flush extends Command {
  /**
   * Default constructor.
   */
  public Flush() {
    super(Perm.WRITE, true);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(!options.get(MainOptions.AUTOFLUSH)) data.flush(true);
    return info(DB_FLUSHED_X, data.meta.name, jc().performance);
  }

  @Override
  public void addLocks() {
    jc().locks.writes.add(Locking.CONTEXT);
  }
}
