package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
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
    super(STANDARD, file);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    Prop.xquery = io;

    try {
      return query(Token.string(io.content())) && out(out);
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public boolean write() {
    try {
      return updating(Token.string(IO.get(args[0]).content()));
    } catch(final IOException ex) {
      return super.write();
    }
  }
}
