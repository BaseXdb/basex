package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.io.IO;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Evaluates the 'run' command and processes a query file as XQuery.
 *
 * @author BaseX Team 2005-11, BSD License
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
      return error(FILEWHICH, context.user.perm(User.CREATE) ? io : args[0]);

    context.prop.set(Prop.QUERYPATH, io.path());
    try {
      return query(Token.string(io.read()));
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    try {
      return updating(ctx, Token.string(IO.get(args[0]).read()));
    } catch(final IOException ex) {
      return true;
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(0);
  }
}
