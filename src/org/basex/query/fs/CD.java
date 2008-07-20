package org.basex.query.fs;

import java.io.IOException;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;

/**
 * Performs a cd command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class CD extends FSCmd {
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args, final int pos) throws FSException {
    // check default options and get path
    path = defaultOpts(args, pos).getPath();
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    // if there is path expression go to work
    if(path != null) {
      curPre = checkPre(path, FSUtils.goToDir(context.data(), curPre, path));
      context.current(new Nodes(curPre, context.data()));
    } else {
      context.current(new Nodes(FSUtils.getROOTDIR(), context.data()));
    }
  }
}
