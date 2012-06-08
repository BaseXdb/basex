package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;

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
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.DATABASES);
  }
}
