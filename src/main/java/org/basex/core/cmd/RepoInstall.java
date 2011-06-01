package org.basex.core.cmd;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.query.QueryException;
import org.basex.query.util.repo.RepoManager;
import org.basex.util.InputInfo;

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
    super(STANDARD, p);
    ii = i;
  }

  @Override
  protected boolean run() throws IOException {
    try {
      new RepoManager(context).install(args[0], ii);
      return true;
    } catch(final QueryException ex) {
      return error(ex.getMessage());
    }
  }
}
