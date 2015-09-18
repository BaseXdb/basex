package org.basex.core.cmd;

import static org.basex.util.Strings.*;

import java.io.*;

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
    // get arguments
    final String start = args[0];
    final String end = args[1];

    DBNodes nodes = null;
    if(!start.isEmpty() && toInt(start) == Integer.MIN_VALUE) {
      try {
        // evaluate input as query
        final Value value = qp(args[0], context).value();
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
      int ps = 0, pe = 1000;
      if(!start.isEmpty()) {
        if(!end.isEmpty()) {
          ps = toInt(start);
          pe = toInt(end) + 1;
        } else {
          ps = toInt(start);
          pe = ps + 1;
        }
      }
      dp.add(ps, pe);
    }
    out.print(dp.finish());
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CONTEXT);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.STORAGE);
    if(!args[0].isEmpty() && toInt(args[0]) == Integer.MIN_VALUE) {
      cb.xquery(0);
    } else {
      cb.arg(0).arg(1);
    }
  }
}
