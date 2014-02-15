package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;

/**
 * Evaluates the 'flush' command and flushes the database buffers.
 *
 * @author BaseX Team 2005-14, BSD License
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
    if(!options.get(MainOptions.AUTOFLUSH)) {
      options.set(MainOptions.AUTOFLUSH, true);
      data.finishUpdate();
      options.set(MainOptions.AUTOFLUSH, false);
    }
    return info(DB_FLUSHED_X, data.meta.name, perf);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.CTX);
  }
}
