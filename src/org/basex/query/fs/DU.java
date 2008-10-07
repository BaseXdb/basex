package org.basex.query.fs;

import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.Token;

/**
 * Performs a du command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class DU extends FSCmd {
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
    final GetOpts g = new GetOpts(args, "ahHs");
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'a':
          fPrintAll = true;
          break;
        case 's':
          fSum = true;
          break;
        case 'H':
          fHuman = true;
          break;
      }
    }
    path = g.getPath();
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    // if there is path expression go to dir
    if(path != null) {
      final int[] sources = checkPre(path,
          FSUtils.getChildren(context.data(), curPre, path));

      du(sources, out, true);
    } else {
      du(new int[] { curPre }, out, true);
    }
  }

  /**
   * The du utility displays the file system block usage for each file argu-
   * ment and for each directory in the file hierarchy rooted in each direc-
   * tory argument.  If no file is specified, the block usage of the hierarchy
   * rooted in the current directory is displayed.
   *
   * @param sources pre values of the nodes to print
   * @param out output stream
   * @param top top flag
   * @throws IOException in case of problems with the PrintOutput
   * @return memory usage
   */
  private long du(final int[] sources, final PrintOutput out, final boolean top)
      throws IOException {

    long size = 0;
    for(final int pre : sources) {
      if(FSUtils.isFile(data, pre)) {
        if(!fSum || top)
          print(out, Token.toLong(FSUtils.getSize(data, pre)), Token.concat(
              FSUtils.getRelativePath(data, pre, curPre),
              FSUtils.getName(data, pre)));
      } else {
        final DirIterator it = new DirIterator(data, pre);
        long sz = Token.toLong(FSUtils.getSize(data, pre));

        while(it.more()) {
          final int n = it.next();
          if(FSUtils.isDir(data, n)) {
            sz += du(new int[]{ n }, out, false);
          } else {
            final long s = Token.toLong(FSUtils.getSize(data, n));
            if(fPrintAll && (!fSum || top)) {
              print(out, s, Token.concat(
                  FSUtils.getRelativePath(data, pre, curPre), Token.token('/'),
                  FSUtils.getName(data, n)));
            }
            sz += s;
          }
        }
        if(!fSum || top)
          print(out, sz, FSUtils.getRelativePath(data, pre, curPre));
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
    out.print(12, Token.token(fHuman ? format(s) : Long.toString(s)));
    out.print("  ");
    out.println(pt);
  }
}
