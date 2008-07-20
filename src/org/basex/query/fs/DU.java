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
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args, final int pos) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "ah", pos);
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'a':
          fPrintAll = true;
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

      du(sources, out);
    } else {
      du(new int[] { curPre }, out);
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
   * @throws IOException in case of problems with the PrintOutput
   * @return memory usage
   */
  private long du(final int[] sources, final PrintOutput out)
      throws IOException {

    for(final int pre : sources) {
      if(FSUtils.isFile(data, pre)) {
        out.print(FSUtils.getSize(data, pre) + "\t" +
            Token.string(FSUtils.getRelativePath(data, pre, curPre)) +
            Token.string(FSUtils.getName(data, pre)) + "\r");
      } else {
        final DirIterator it = new DirIterator(data, pre);
        long diskusage = FSUtils.getSize(data, pre);

        while(it.more()) {
          final int n = it.next();
          if(FSUtils.isDir(data, n)) {
            diskusage += du(new int[]{ n }, out);
          } else {
            final long diskuse = FSUtils.getSize(data, n);
            if(fPrintAll) {
              out.print(diskuse + "\t" +
                  Token.string(FSUtils.getRelativePath(data, pre, curPre))
                  + "/" + Token.string(FSUtils.getName(data, n)) + "\r");
            }
            diskusage += diskuse;

          }
        }
        out.println(diskusage + "\t" +
            Token.string(FSUtils.getRelativePath(data, pre, curPre)));
        return diskusage;
      }
    }
    return -1;
  }
}
