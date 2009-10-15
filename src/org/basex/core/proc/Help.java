package org.basex.core.proc;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Process;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'help' command and returns help on the database commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Help extends Process {
  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Help(final String arg) {
    super(STANDARD, arg);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    try {
      final Cmd cmd = Cmd.valueOf(args[0]);
      out.print(cmd.help(true));
    } catch(final Exception ex) {
      out.println(CMDHELP);
      for(final Cmd c : Cmd.values()) out.print(c.help(false));
    }
    return true;
  }
}
