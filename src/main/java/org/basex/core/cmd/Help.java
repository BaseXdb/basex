package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Command;

/**
 * Evaluates the 'help' command and returns help on the database commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Help extends Command {
  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Help(final String arg) {
    this(arg, null);
  }

  /**
   * Default constructor.
   * @param arg optional argument
   * @param format optional format (e.g., Wiki)
   */
  public Help(final String arg, final String format) {
    super(STANDARD, arg, format);
  }

  @Override
  protected boolean run() throws IOException {
    final String key = args[0];
    final boolean wiki = args[1] != null;

    if(key != null) {
      final Cmd cmd = getOption(key, Cmd.class);
      if(cmd == null) return error(CMDUNKNOWN, this);
      out.print(cmd.help(true, wiki));
    } else {
      out.println(CMDHELP);
      for(final Cmd c : Cmd.values()) out.print(c.help(false, wiki));
    }
    return true;
  }
}
