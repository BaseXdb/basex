package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Command;
import org.basex.core.Commands;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'help' command. Checks the help command arguments.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Help extends Proc {
  /** Command. */
  private Command com;
  /** Show all commands. */
  private boolean all;
  
  @Override
  protected boolean exec() {
    try {
      all = cmd.args().equalsIgnoreCase("all");
      if(!all && cmd.nrArgs() != 0) com = new Command(cmd.arg(0));
      return true;
    } catch(final IllegalArgumentException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    // print general help, if no argument is given
    if(com == null) {
      out.println(CMDHELP);
      for(final Commands c : Commands.values()) out.print(c.help(false, all));
    } else {
      out.print(com.name.help(true, true));
    }
  }
}
