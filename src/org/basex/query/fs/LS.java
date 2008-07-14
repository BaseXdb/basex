package org.basex.query.fs;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Performs a ls command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class LS extends FSCmd {
  /** Also lists sub directories. */
  private boolean fRecursive;
  /** Lists files beginning with . */
  private boolean fListDot;
  /** Prints long version. */
  private boolean fPrintLong;
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "ahlR", 1);
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'a':
          fListDot = true;
          break;
        case 'l':
          fPrintLong = true;
          break;
        case 'R':
          fRecursive = true;
          break;
      }
    }
    path = g.getPath();
  }

  @Override
  public void exec(final String cmd, final PrintOutput out) throws IOException {
    // if there is path expression set new pre value
    if(path != null) {
      curPre = checkPre(path, FSUtils.goToDir(data, curPre, path));
    }

    // go to work
    if(fRecursive) {
      lsRecursive(curPre, out);
    } else {
      print(FSUtils.getChildren(data, curPre), out);
    }
  }

  /**
   * Recursively lists sub directories encountered.
   *
   * @param pre Value of dir
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void lsRecursive(final int pre, final PrintOutput out)
      throws IOException {

    final int[] contentDir = FSUtils.getChildren(data, pre);
    final int[] allDir = print(contentDir, out);

    for(final int dir : allDir) {
      if(!fListDot) {
        // don´t crawl dirs starting with ´.´
        if(Token.startsWith(FSUtils.getName(data, dir), '.')) continue;
      }
      out.print(NL);
      out.print(FSUtils.getPath(data, dir));
      out.print(NL);
      lsRecursive(dir, out);
    }
  }

  /**
   * Prints the result.
   * @param result - array to print
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   * @return list of directories found
   */
  private int[] print(final int[] result, final PrintOutput out)
      throws IOException {
    
    final IntList allDir = new IntList();
    for(final int j : result) {
      if(FSUtils.isDir(data, j)) allDir.add(j);
      final byte[] name = FSUtils.getName(data, j);
      // do not print files starting with .
      if(!fListDot && Token.startsWith(name, '.')) continue;
      
      if(fPrintLong) {
        final long size = FSUtils.getSize(data, j);
        final byte[] time = FSUtils.getMtime(data, j);
        final char file = FSUtils.isFile(data, j) ? 'f' : 'd';
        out.println(String.format("%-3s %-30s %10s %20s", file,
            Token.string(name), size, Token.string(time)));
      } else {
        out.print(name);
        out.print("\t");
      }
    }
    if(!fPrintLong) out.print(NL);
    return allDir.finish();
  }
}
