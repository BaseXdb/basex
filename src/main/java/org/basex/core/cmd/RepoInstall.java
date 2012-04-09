package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdRepo;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;

/**
 * Evaluates the 'repo install' command.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class RepoInstall extends Command {
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
      final boolean exists = new RepoManager(context, info).install(Token.token(args[0]));
      return info(exists ? PKG_REPLACED_X_X : PKG_INSTALLED_X_X, args[0], perf);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.INSTALL).args();
  }
}
