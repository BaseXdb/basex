package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Command;

/**
 * Evaluates the 'help' command and returns help on the database commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    final String in = args[0];
    final boolean wiki = args[1] != null;

    if(in != null) {
      final Cmd cmd = Cmd.valueOf(in);
      out.print(cmd.help(true, wiki));
    } else {
      out.println(CMDHELP);
      for(final Cmd c : Cmd.values()) out.print(c.help(false, wiki));
    }
    return true;
  }
}
