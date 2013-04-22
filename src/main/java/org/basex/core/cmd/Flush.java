package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;

/**
 * Evaluates the 'flush' command and flushes the database buffers.
 *
 * @author BaseX Team 2005-12, BSD License
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
    if(!prop.is(Prop.AUTOFLUSH)) {
      prop.set(Prop.AUTOFLUSH, true);
      data.finishUpdate();
      prop.set(Prop.AUTOFLUSH, false);
    }
    return info(DB_FLUSHED_X, data.meta.name, perf);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.write.add(DBLocking.CTX);
  }
}
