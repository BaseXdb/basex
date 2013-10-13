package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;

/**
 * Evaluates the 'repo install' command.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 */
public final class RepoInstall extends ARepo {
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param p package
   * @param i input info
   */
  public RepoInstall(final String p, final InputInfo i) {
    super(Perm.CREATE, p);
    info = i;
  }

  @Override
  protected boolean run() throws IOException {
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
