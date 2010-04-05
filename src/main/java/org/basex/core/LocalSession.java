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
  /** Database Context. */
  private final Context ctx;
  /** Process reference. */
  private Proc proc;

  /**
   * Constructor.
   * @param context context
   */
  public LocalSession(final Context context) {
    ctx = context;
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
    return pr.exec(ctx, out);
  }

  @Override
  public String info() {
    return proc.info();
  }

  @Override
  public void close() {
  }
}
