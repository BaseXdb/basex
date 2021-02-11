package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.users.*;

/**
 * Evaluates the 'help' command and returns help on the database commands.
 *
 * @author BaseX Team 2005-21, BSD License
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
    if(key.isEmpty()) {
      out.println(TRY_SPECIFIC_X);
      for(final Cmd cmd : Cmd.values()) out.print(cmd.help(false));
    } else {
      final Cmd cmd = getOption(key, Cmd.class);
      if(cmd == null) return error(UNKNOWN_CMD_X, this);
      out.println(cmd.help(true));
    }
    return true;
  }

  @Override
  public void addLocks() {
    // no locks needed
  }
}
