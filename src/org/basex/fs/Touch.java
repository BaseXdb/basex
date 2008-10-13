package org.basex.fs;

import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Performs a touch command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Touch extends FSCmd {
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args) throws FSException {
    // check default options and get path
    path = defaultOpts(args).getPath();
    // no file/path was specified...
    if(path == null) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    final int i = path.lastIndexOf('/');
    String file = path;
    if(i > -1) {
      goTo(path.substring(0, i));
      file = path.substring(i + 1);
    }

    final int[] nodes =  fs.children(curPre, file);
    if(nodes.length > 0) {
      // refresh time stamps
      for(final int p : nodes) {
        fs.time(p, fs.currTime());
      }
    } else {
      // add new file
      if(!fs.valid(file)) error(file, 101);

      // add new entry at the end of directory
      final int pn = curPre + fs.data.size(curPre, fs.data.kind(curPre));
      fs.insert(false, Token.token(file), getSuffix(file), Token.ZERO,
          fs.currTime(), curPre, pn);
      fs.flush();
    }
  }

  /**
   * Extracts the suffix of a file.
   * @param file the filename
   * @return the suffix of the file
   */
  private byte[] getSuffix(final String file) {
    final int i = file.lastIndexOf('.');
    return i > 0 ? Token.token(file.substring(i + 1)) : Token.EMPTY;
  }
}