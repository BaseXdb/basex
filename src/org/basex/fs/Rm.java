package org.basex.fs;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * Performs a rm command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Rm extends FSCmd {
  /** Remove the all. */
  private boolean fRecursive;
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args) throws FSException {
    // get all Options
    final FSGetOpts g = new FSGetOpts(args, "R");
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'R':
          fRecursive = true;
          break;
      }
    }
    path = g.getPath();
    if(path == null) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    // get nodes to be deleted
    final int[] nodes = children(path);

    // nodes are deleted backwards
    for(int n = nodes.length - 1; n >= 0; n--) {
      final int pre = nodes[n];
      if(fs.isFile(pre) || fs.isDir(pre) && fRecursive) {
        fs.data.delete(pre);
      } else {
        warning(out, fs.name(pre), 21);
      }
    }
    fs.flush();
  }
}
