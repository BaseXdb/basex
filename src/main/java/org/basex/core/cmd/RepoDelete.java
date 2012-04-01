package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdRepo;
import org.basex.query.QueryException;
import org.basex.query.util.pkg.RepoManager;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Evaluates the 'repo delete' command.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class RepoDelete extends Command {
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param p package
   * @param i input info
   */
  public RepoDelete(final String p, final InputInfo i) {
    super(User.CREATE, p);
    info = i;
  }

  @Override
  protected boolean run() throws IOException {
    try {
      new RepoManager(context, info).delete(args[0]);
      return info(PKG_DELETED_X, args[0]);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.DELETE).args();
  }
}
