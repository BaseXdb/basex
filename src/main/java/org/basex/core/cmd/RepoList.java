package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.query.util.pkg.*;
import org.basex.util.list.*;

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
    super(Perm.NONE);
  }

  @Override
  protected boolean run() throws IOException {
    out.println(new RepoManager(context).table().finish());
    return true;
  }

  @Override
  protected boolean databases(final StringList db) {
    return true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.LIST).args();
  }
}
