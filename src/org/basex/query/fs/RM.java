package org.basex.query.fs;

import java.io.IOException;

import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Performs a rm command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class RM extends FSCmd {
  /** Remove the all. */
  private boolean fRecursive;
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args, final int pos) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "Rh", pos);
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'R':
          fRecursive = true;
          break;
      }
    }
    path = g.getPath();
    // no file/path was specified...
    if(path == null) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    final int[] del = checkPre(path, FSUtils.getChildren(data, curPre, path));

    // following Pre Values change if nodes are deleted -> delete backwards
    for(int d = del.length - 1; d >= 0; d--) {
      final int toDel = del[d];
      if((FSUtils.isDir(data, toDel) && fRecursive) ||
          (FSUtils.isFile(data, toDel))) {
        FSUtils.delete(data, toDel);
      } else {
        error(path, 21);
      }
    }
  }
}
