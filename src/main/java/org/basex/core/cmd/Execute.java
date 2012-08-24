package org.basex.core.cmd;

import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.query.*;

/**
 * Evaluates the 'execute' command and runs a command script.
 * This command can be used to run multiple commands as a single transaction.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class Execute extends Command {
  /** Commands to execute. */
  protected final ArrayList<Command> list = new ArrayList<Command>();
  /** Error message. */
  protected String error;

  /**
   * Default constructor.
   * @param input user input
   */
  public Execute(final String input) {
    super(Perm.NONE, false, input);
  }

  @Override
  protected boolean run() {
    if(!init(context)) return error(error);

    final StringBuilder sb = new StringBuilder();
    for(final Command c : list) {
      progress(c);
      final boolean ok = c.run(context, out);
      sb.append(c.info());
      if(!ok) return error(sb.toString());
    }
    return info(sb.toString().replaceAll("\r?\n?$", ""));
  }

  @Override
  public boolean updating(final Context ctx) {
    if(!init(ctx)) return true;
    for(final Command c : list) {
      if(!c.updating(ctx)) return true;
    }
    return false;
  }

  /**
   * Initializes the specified input.
   * @param ctx database context
   * @return success flag
   */
  protected boolean init(final Context ctx) {
    if(list.isEmpty() && error == null) {
      try {
        // interpret at commands if input starts with < or ends with command script suffix
        for(final Command c : new CommandParser(args[0], ctx).parse()) list.add(c);
      } catch(final QueryException ex) {
        error = ex.getMessage();
        return false;
      }
    }
    return error == null;
  }

  @Override
  public final void build(final CmdBuilder cb) {
    cb.init().arg(0);
  }
}
