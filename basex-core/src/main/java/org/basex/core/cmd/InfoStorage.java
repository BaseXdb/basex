package org.basex.core.cmd;

import static org.basex.util.Strings.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;

/**
 * Evaluates the 'info storage' command and returns the table representation
 * of the currently opened database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InfoStorage extends AInfo {
  /**
   * Default constructor.
   * @param arg arguments (the passed on argument can be {@code null})
   */
  public InfoStorage(final String... arg) {
    super(true, arg.length > 0 && arg[0] != null ? arg[0] : "",
                arg.length > 1 && arg[1] != null ? arg[1] : "");
  }

  @Override
  protected boolean run() throws IOException {
    final String first = args[0], second = args[1];

    final Data data = context.data();
    final DataPrinter dp = new DataPrinter(data);
    int start = 0, end = 1000;
    if(!first.isEmpty()) {
      start = toInt(first);
      end = (second.isEmpty() ? start : toInt(second)) + 1;
    }
    dp.add(start, end);
    out.print(dp.finish());
    return true;
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.STORAGE).arg(0).arg(1);
  }
}
