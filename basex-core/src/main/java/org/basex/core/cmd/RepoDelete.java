package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdRepo;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;

/**
 * Evaluates the 'repo delete' command.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class RepoDelete extends ARepo {
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param pkg package
   * @param info input info (can be {@code null})
   */
  public RepoDelete(final String pkg, final InputInfo info) {
    super(Perm.ADMIN, pkg);
    this.info = info;
  }

  @Override
  protected boolean run() {
    try {
      new RepoManager(context, info).delete(args[0]);
      return info(PKG_DELETED_X, args[0]);
    } catch(final QueryException ex) {
      return error(ex);
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.DELETE).args();
  }
}
