package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Evaluates the 'run' command and processes a query file as XQuery.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Run extends AQuery {
  /**
   * Default constructor.
   * @param file query file
   */
  public Run(final String file) {
    super(STANDARD, file);
  }

  @Override
  protected boolean run() {
    final IO io = IO.get(args[0]);
    if(!io.exists())
      return error(FILEWHICH, context.user.perm(User.CREATE) ? io : io.name());

    context.query = io;

    try {
      return query(Token.string(io.content()));
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    try {
      return updating(ctx, Token.string(IO.get(args[0]).content()));
    } catch(final IOException ex) {
      return true;
    }
  }
}
