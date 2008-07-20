package org.basex.query.fs;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * Performs a pwd command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class PWD extends FSCmd {
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args, final int pos) throws FSException {
    // check default options and get path
    path = defaultOpts(args, pos).getPath();
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    // if there is path expression go to dir
    if(path != null) {
      curPre = checkPre(path, FSUtils.goToDir(data, curPre, path));
    }
    out.println(FSUtils.getPath(data, curPre));
  }
}
