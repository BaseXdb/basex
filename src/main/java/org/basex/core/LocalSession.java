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
  /** Database context. */
  private final Context ctx;
  /** Command reference. */
  private Command cmd;

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
  public boolean execute(final Command c, final OutputStream out) {
    cmd = c;
    return c.exec(ctx, out);
  }

  @Override
  public String info() {
    return cmd.info();
  }

  @Override
  public void close() {
  }
}
