package org.basex.core.cmd;

import static org.basex.util.Strings.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdInfo;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Evaluates the 'info storage' command and returns the table representation
 * of the currently opened database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class InfoStorage extends AQuery {
  /**
   * Default constructor.
   * @param arg arguments (can be {@code null})
   */
  public InfoStorage(final String... arg) {
    super(Perm.READ, true, arg.length > 0 && arg[0] != null ? arg[0] : "",
                           arg.length > 1 && arg[1] != null ? arg[1] : "");
  }

  @Override
  protected boolean run() throws IOException {
    final String first = args[0], second = args[1];
    DBNodes nodes = null;
    if(isQuery()) {
      try {
        // evaluate input as query
        final Value value = qp(first, context).value();
        if(value instanceof DBNodes) nodes = (DBNodes) value;
      } catch(final QueryException ex) {
        error(Util.message(ex));
      } finally {
        closeQp();
      }
    }

    final Data data = context.data();
    final DataPrinter dp = new DataPrinter(data);
    if(nodes != null) {
      for(final int pre : nodes.pres()) dp.add(pre);
    } else {
      int start = 0, end = 1000;
      if(!first.isEmpty()) {
        if(!second.isEmpty()) {
          start = toInt(first);
          end = toInt(second) + 1;
        } else {
          start = toInt(first);
          end = start + 1;
        }
      }
      dp.add(start, end);
    }
    out.print(dp.finish());
    return true;
  }

  @Override
  public boolean updating(final Context ctx) {
    return isQuery() && updates(ctx, args[0]);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CONTEXT);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.STORAGE);
    if(isQuery()) cb.xquery(0);
    else cb.arg(0).arg(1);
  }

  /**
   * Checks if the first argument is a query.
   * @return result of check
   */
  private boolean isQuery() {
    final String first = args[0];
    return !first.isEmpty() && toInt(first) == Integer.MIN_VALUE;
  }
}
