package org.basex.server;

import java.io.OutputStream;
import org.basex.core.Context;
import org.basex.core.LocalSession;
import org.basex.core.Proc;

/**
 * This wrapper executes server commands locally.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ServerSession extends LocalSession {
  /** Server reference. */
  private final Semaphore sema;

  /**
   * Constructor.
   * @param context context
   * @param s semaphore reference
   */
  public ServerSession(final Context context, final Semaphore s) {
    super(context);
    sema = s;
  }

  @Override
  public boolean execute(final Proc pr, final OutputStream out) {
    final boolean w = sema.writing(pr, ctx);
    sema.before(w);
    final boolean ok = super.execute(pr, out);
    sema.after(w);
    return ok;
  }
}
