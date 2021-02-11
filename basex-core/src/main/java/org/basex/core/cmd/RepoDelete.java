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
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class RepoDelete extends ARepo {
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param perm package
   * @param info input info
   */
  public RepoDelete(final String perm, final InputInfo info) {
    super(Perm.CREATE, perm);
    this.info = info;
  }

  @Override
  protected boolean run() {
    try {
      new RepoManager(context, info).delete(args[0]);
      return info(PKG_DELETED_X, args[0]);
    } catch(final QueryException ex) {
      return error(Util.message(ex));
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.DELETE).args();
  }
}
