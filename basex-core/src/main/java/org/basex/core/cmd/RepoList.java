package org.basex.core.cmd;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdRepo;
import org.basex.core.users.*;
import org.basex.query.util.pkg.*;

/**
 * Evaluates the 'repo list' command.
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 */
public final class RepoList extends ARepo {
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
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.REPO);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.LIST).args();
  }
}
