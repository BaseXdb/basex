package org.basex.core.cmd;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.util.RepoManager;
import org.basex.util.InputInfo;

/**
 * Evaluates the 'repo install' command.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class InstallPkg extends Command {

  /** Package to be installed. */
  private String pkg;
  /** Context. */
  private Context ctx;
  /** Input info. */
  private InputInfo ii;

  /**
   * Constructor.
   * @param p package
   * @param c context
   * @param i input info
   */
  public InstallPkg(final String p, final Context c, final InputInfo i) {
    super(STANDARD);
    this.pkg = p;
    this.ctx = c;
    this.ii = i;
  }

  @Override
  protected boolean run() throws IOException {
    RepoManager rm = new RepoManager(ctx, ii);
    try {
      rm.installPackage(pkg);
    } catch(QueryException e) {
      error(e.getMessage());
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
