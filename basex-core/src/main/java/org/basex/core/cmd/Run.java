package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Evaluates the 'run' command and processes an input file.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Run extends Execute {
  /** Input reference. */
  private IO file;

  /**
   * Default constructor.
   * @param fl input file
   */
  public Run(final String fl) {
    super(fl);
  }

  /**
   * Initializes the specified input.
   * @param ctx database context
   * @return success flag
   */
  @Override
  protected boolean init(final Context ctx) {
    if(file == null) {
      // check file reference
      file = uri.isEmpty() ? IO.get(args[0]) : IO.get(uri).merge(args[0]);
      if(!file.exists() || file.isDir()) {
        error = Util.info(RES_NOT_FOUND_X, ctx.user().has(Perm.CREATE) ? file : args[0]);
      } else {
        try {
          // interpret as commands if input ends with command script suffix
          if(file.hasSuffix(IO.BXSSUFFIX)) return init(file.string(), file.path(), ctx);
          // otherwise, interpret input as xquery
          commands.add(new XQuery(file.string()).baseURI(file.path()));
        } catch(final IOException ex) {
          error = Util.message(ex);
        }
      }
    }
    return error == null;
  }
}
