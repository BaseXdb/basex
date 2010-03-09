package org.basex.core;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.query.QueryException;

/**
 * This wrapper executes commands locally.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class LocalSession extends Session {
  /** Server reference. */
  private final Semaphore sema;
  /** Database Context. */
  protected final Context ctx;
  /** Process reference. */
  protected Proc proc;

  /**
   * Constructor.
   * @param context context
   */
  public LocalSession(final Context context) {
    this(context, new Semaphore());
  }

  /**
   * Constructor.
   * @param context context
   * @param semaphore instance
   */
  public LocalSession(final Context context, final Semaphore semaphore) {
    ctx = context;
    sema = semaphore;
  }

  @Override
  public boolean execute(final String str, final OutputStream out)
      throws IOException {

    try {
      return execute(new CommandParser(str, ctx).parse()[0], out);
    } catch(final QueryException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public boolean execute(final Proc pr, final OutputStream out) {
    proc = pr;
    final boolean w = sema.writing(pr, ctx);
    sema.before(w);
    final boolean ok = pr.exec(ctx, out);
    sema.after(w);
    return ok;
  }

  @Override
  public String info() {
    return proc.info();
  }

  @Override
  public void close() {
  }
}
