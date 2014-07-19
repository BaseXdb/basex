package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdRepo;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;

/**
 * Evaluates the 'repo install' command.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class RepoInstall extends ARepo {
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param perm package
   * @param info input info
   */
  public RepoInstall(final String perm, final InputInfo info) {
    super(Perm.CREATE, perm);
    this.info = info;
  }

  @Override
  protected boolean run() {
    try {
      final boolean exists = new RepoManager(context, info).install(args[0]);
      return info(exists ? PKG_REPLACED_X_X : PKG_INSTALLED_X_X, args[0], perf);
    } catch(final QueryException ex) {
      return error(Util.message(ex));
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.INSTALL).args();
  }
}
