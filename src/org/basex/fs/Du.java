package org.basex.fs;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.query.ChildIterator;
import org.basex.util.GetOpts;

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
    final GetOpts g = new GetOpts(args, "ahs");
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
    if(path != null) curPre(path);
    du(new int[] { curPre }, out, true);
  }

  /**
   * The du utility displays the file system block usage for each file argu-
   * ment and for each directory in the file hierarchy rooted in each direc-
   * tory argument.  If no file is specified, the block usage of the hierarchy
   * rooted in the current directory is displayed.
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
      if(fs.isFile(p)) {
        if(!fSum || top) print(out, toLong(fs.size(p)),
            concat(fs.path(p, curPre, false), fs.name(p)));
      } else {
        final ChildIterator it = new ChildIterator(fs.data, p);
        long sz = 0;

        while(it.more()) {
          final int n = it.next();
          if(fs.isDir(n)) {
            sz += du(new int[] { n }, out, false);
          } else {
            final long s = toLong(fs.size(n));
            if(fPrintAll && (!fSum || top)) print(out, s,
                concat(fs.path(p, curPre, false), token('/'), fs.name(n)));
            sz += s;
          }
        }

        if(!fSum || top) print(out, sz, fs.path(p, curPre, false));
        size += sz;
      }
    }
    return size;
  }
  
  /**
   * Prints the specified size and path.
   * @param out output reference
   * @param s size
   * @param pt path
   * @throws IOException I/O exception
   */
  private void print(final PrintOutput out, final long s, final byte[] pt)
      throws IOException {
    out.print(token(fHuman ? format(s) : Long.toString(s)), 6);
    out.print("  ");
    out.println(pt);
  }
}
