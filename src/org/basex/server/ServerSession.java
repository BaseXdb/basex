package org.basex.server;

import java.io.OutputStream;

import org.basex.core.Context;
import org.basex.core.LocalSession;
import org.basex.core.Proc;
import org.basex.core.User;

/**
 * This wrapper executes server commands locally.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class ServerSession extends LocalSession {
  /** Server reference. */
  private final Semaphore sem;

  /**
   * Constructor.
   * @param context context
   * @param s semaphore reference
   */
  public ServerSession(final Context context, final Semaphore s) {
    super(context);
    sem = s;
  }

  @Override
  public boolean execute(final Proc pr, final OutputStream out) {
    final boolean up = pr.updating(ctx) || (pr.flags & User.CREATE) != 0;
    sem.before(up);
    final boolean ok = super.execute(pr, out);
    sem.after(up);
    return ok;
  }
}
