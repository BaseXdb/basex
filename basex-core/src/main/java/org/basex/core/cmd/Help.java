package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.users.*;

/**
 * Evaluates the 'help' command and returns help on the database commands.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Help extends Command {
  /**
   * Default constructor.
   * @param arg argument (can be {@code null})
   */
  public Help(final String arg) {
    super(Perm.NONE, arg == null ? "" : arg);
  }

  @Override
  protected boolean run() throws IOException {
    final String key = args[0];
    if(!key.isEmpty()) {
      final Cmd cmd = getOption(key, Cmd.class);
      if(cmd == null) return error(UNKNOWN_CMD_X, this);
      out.println(cmd.help(true));
    } else {
      out.println(TRY_SPECIFIC_X);
      for(final Cmd c : Cmd.values()) out.print(c.help(false));
    }
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    // No locks needed
  }
}
