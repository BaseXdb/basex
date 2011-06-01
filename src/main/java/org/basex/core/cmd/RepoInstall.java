package org.basex.core.cmd;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.User;
import org.basex.query.QueryException;
import org.basex.query.util.repo.RepoManager;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Evaluates the 'repo install' command.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class RepoInstall extends Command {
  /** Input info. */
  private final InputInfo ii;

  /**
   * Constructor.
   * @param p package
   * @param i input info
   */
  public RepoInstall(final String p, final InputInfo i) {
    super(User.ADMIN, p);
    ii = i;
  }

  @Override
  protected boolean run() throws IOException {
    try {
      new RepoManager(context).install(args[0], ii);
      return true;
    } catch(final QueryException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }
}
