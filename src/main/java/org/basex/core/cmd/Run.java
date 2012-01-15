package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.BaseXException;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Run extends AQuery {
  /** Query string. */
  String query;

  /**
   * Default constructor.
   * @param file query file
   */
  public Run(final String file) {
    super(STANDARD, file);
  }

  @Override
  protected boolean run() {
    try {
      return query(read(context));
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    try {
      return updating(ctx, read(ctx));
    } catch(final IOException ex) {
      return true;
    }
  }

  /**
   * Returns the query string.
   * @param ctx database context
   * @return query string
   * @throws IOException I/O exception
   */
  protected String read(final Context ctx) throws IOException {
    if(query == null) {
      final IO io = IO.get(args[0]);
      if(!io.exists()) throw new BaseXException(
          FILEWHICH, ctx.user.perm(User.CREATE) ? io : args[0]);
      query = Token.string(io.read());
      ctx.prop.set(Prop.QUERYPATH, io.path());
    }
    return query;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(0);
  }
}
