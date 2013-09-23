package org.basex.core.cmd;

import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.query.*;
import org.basex.util.*;

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
    super(Perm.ADMIN, false, input);
  }

  @Override
  public boolean newData(final Context ctx) {
    return new Close().run(ctx);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.writeAll = true;
  }

  @Override
  protected boolean run() {
    try {
      if(!init(context)) return error(error);

      final StringBuilder sb = new StringBuilder();
      for(final Command c : list) {
        final boolean ok = proc(c).run(context, out);
        proc(null);
        sb.append(c.info());
        if(!ok) return error(sb.toString());
      }
      return info(sb.toString().replaceAll("\r?\n?$", ""));
    } finally {
      finish(context);
    }
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
    return init(args[0], ctx);
  }

  /**
   * Initializes the specified input.
   * @param input command input
   * @param ctx database context
   * @return success flag
   */
  protected final boolean init(final String input, final Context ctx) {
    if(list.isEmpty() && error == null) {
      try {
        Collections.addAll(list, new CommandParser(input, ctx).parse());
      } catch(final QueryException ex) {
        error = Util.message(ex);
        return false;
      }
    }
    return error == null;
  }

  /**
   * Finalizes command execution.
   * @param ctx database context
   */
  @SuppressWarnings("unused")
  protected void finish(final Context ctx) {
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0);
  }
}
