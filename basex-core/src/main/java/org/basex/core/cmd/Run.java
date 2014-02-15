package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Evaluates the 'run' command and processes an input file.
 *
 * @author BaseX Team 2005-14, BSD License
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
      file = IO.get(args[0]);
      if(!file.exists() || file.isDir()) {
        error = Util.info(RES_NOT_FOUND_X, ctx.user.has(Perm.CREATE) ? file : args[0]);
      } else {
        try {
          // retrieve file contents
          final String input = file.string();
          // interpret as commands if input ends with command script suffix
          if(file.hasSuffix(IO.BXSSUFFIX)) return init(input, ctx);
          // otherwise, interpret input as xquery
          list.add(new XQuery(input));
        } catch(final IOException ex) {
          error = Util.message(ex);
        }
      }
    }
    ctx.options.set(MainOptions.QUERYPATH, file.path());
    return error == null;
  }

  @Override
  protected void finish(final Context ctx) {
    ctx.options.set(MainOptions.QUERYPATH, "");
  }
}
