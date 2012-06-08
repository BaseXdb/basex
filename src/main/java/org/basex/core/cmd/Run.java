package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Evaluates the 'run' command and processes a query file as XQuery.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Run extends AQuery {
  /** Query string. */
  private String query;

  /**
   * Default constructor.
   * @param file query file
   */
  public Run(final String file) {
    super(Perm.NONE, false, file);
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
  String read(final Context ctx) throws IOException {
    if(query == null) {
      final IO io = IO.get(args[0]);
      if(!io.exists() || io.isDir()) throw new BaseXException(
          RES_NOT_FOUND_X, ctx.user.has(Perm.CREATE) ? io : args[0]);
      query = Token.string(io.read());
      ctx.prop.set(Prop.QUERYPATH, io.path());
    }
    return query;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0);
  }
}
