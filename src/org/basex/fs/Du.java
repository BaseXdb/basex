package org.basex.fs;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * Performs a du command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Du extends FSCmd {
  /** Display an entry for each file in the file hierarchy. */
  private boolean fPrintAll;
  /** Summarizes the sizes. */
  private boolean fSum;
  /** Human readable format. */
  private boolean fHuman;
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args) throws FSException {
    // get all Options
    final FSGetOpts g = new FSGetOpts(args, "ahs");
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'a':
          fPrintAll = true;
          break;
        case 'h':
          fHuman = true;
          break;
        case 's':
          fSum = true;
          break;
      }
    }
    path = g.getPath();
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    du(path != null ? children(path) : new int[] { curPre }, out, true);
  }

  /**
   * The du utility displays the file system block usage for each file
   * argument and for each directory in the file hierarchy rooted in each
   * directory argument.  If no file is specified, the block usage of the
   * hierarchy rooted in the current directory is displayed.
   *
   * @param nodes pre values of the nodes to print
   * @param out output stream
   * @param top top flag
   * @throws IOException in case of problems with the PrintOutput
   * @return memory usage
   */
  private long du(final int[] nodes, final PrintOutput out, final boolean top)
      throws IOException {

    long size = 0;
    for(final int p : nodes) {
      final boolean f = fs.isFile(p);
      final long s = f ? toLong(fs.size(p)) : du(fs.children(p), out, false);

      if(top || f && fPrintAll || !f && !fSum) {
        out.print(token(fHuman ? format(s) : Long.toString(s)), 8);
        out.println(fs.path(p, curPre, false));
      }
      size += s;
    }
    return size;
  }
}
