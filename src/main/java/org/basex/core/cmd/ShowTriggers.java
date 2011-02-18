package org.basex.core.cmd;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;

/**
 * Evaluates the 'show triggers' command and lists all existing triggers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
 */
public final class ShowTriggers extends Command {
  /**
   * Default constructor.
   */
  public ShowTriggers() {
    super(User.ADMIN);
  }

  @Override
  protected boolean run() {
    try {
      out.write(context.triggers.info());
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.TRIGGERS).args();
  }
}
