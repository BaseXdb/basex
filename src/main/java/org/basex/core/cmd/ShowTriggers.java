package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'show triggers' command and lists all existing triggers.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class ShowTriggers extends Command {
  /**
   * Default constructor.
   */
  public ShowTriggers() {
    super(User.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    TokenBuilder tb = new TokenBuilder();
    int size = context.triggers.size();
    if(size == 0) {
      tb.add(size + " Trigger(s)" + DOT);
    } else {
      tb.add(size + " Trigger(s)" + COL);
      tb.add(context.triggers.info());
    }
    out.println(tb.toString());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.TRIGGERS).args();
  }
}
