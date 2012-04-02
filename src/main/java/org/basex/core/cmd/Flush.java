package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.Data;

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
    super(User.WRITE | DATAREF);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    final boolean af = prop.is(Prop.AUTOFLUSH);
    prop.set(Prop.AUTOFLUSH, true);
    data.flush();
    prop.set(Prop.AUTOFLUSH, af);
    return info(DB_FLUSHED_X, data.meta.name, perf);
  }

  @Override
  public String writeLock(final boolean lock, final Context ctx) {
    return null;
  }
}
