package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdRepo;
import org.basex.query.util.pkg.*;

/**
 * Evaluates the 'repo list' command.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class RepoList extends Command {
  /**
   * Constructor.
   */
  public RepoList() {
    super(STANDARD);
  }

  @Override
  protected boolean run() throws IOException {
    out.println(new RepoManager(context).table().finish());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.LIST).args();
  }
}
