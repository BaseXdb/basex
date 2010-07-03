package org.basex.core;

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

  /**
   * Constructor.
   * @param context context
   */
  public LocalSession(final Context context) {
    ctx = context;
  }

  @Override
  public void execute(final String str, final OutputStream out)
      throws BaseXException {

    try {
      execute(new CommandParser(str, ctx).parseSingle(), out);
    } catch(final QueryException ex) {
      throw new BaseXException(ex.getMessage());
    }
  }

  @Override
  public void execute(final Command cmd, final OutputStream out)
      throws BaseXException {
    cmd.execute(ctx, out);
    info = cmd.info();
  }

  @Override
  public void close() {
  }
}
