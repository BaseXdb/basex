package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Evaluates the 'run' command and processes a query file as XQuery.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Run extends AQuery {
  /**
   * Default constructor.
   * @param file query file
   */
  public Run(final String file) {
    super(PRINTING, file);
  }

  @Override
  protected boolean exec() {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    try {
      return query(Token.string(io.content()));
    } catch(final IOException ex) {
      BaseX.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : args[0]);
    }
  }
}
