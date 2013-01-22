package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
    super(Perm.CREATE, p);
    info = i;
  }

  @Override
  protected boolean run() throws IOException {
    try {
      new RepoManager(context, info).delete(Token.token(args[0]));
      return info(PKG_DELETED_X, args[0]);
    } catch(final QueryException ex) {
      return error(Util.message(ex));
    }
  }

  @Override
  protected boolean databases(final StringList db) {
    return true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.DELETE).args();
  }
}
