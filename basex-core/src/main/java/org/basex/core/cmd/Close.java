package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;

/**
 * Evaluates the 'close' command and closes the current database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Close extends Command {
  /**
   * Default constructor.
   */
  public Close() {
    super(Perm.NONE);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(data == null) return true;

    close(data, context);
    context.closeDB();
    return info(DB_CLOSED_X, data.meta.name);
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  /**
   * Closes the specified database.
   * @param data data reference
   * @param ctx database context
   */
  public static void close(final Data data, final Context ctx) {
    synchronized(ctx.datas) { ctx.datas.unpin(data); }
  }

  /**
   * Closes a currently opened database.
   * @param ctx database context
   * @return {@code true} if no database was opened, or if database was closed
   */
  public static boolean close(final Context ctx) {
    return new Close().run(ctx);
  }
}
