package org.basex.core.cmd;

import java.io.IOException;
import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;

/**
 * Evaluates the 'show databases' command and shows opened databases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ShowDatabases extends Command {
  /**
   * Default constructor.
   */
  public ShowDatabases() {
    super(Perm.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    out.println(context.datas.info());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.DATABASES);
  }
}
