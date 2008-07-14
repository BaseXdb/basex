package org.basex.query.fs;

import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Performs a touch command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public class MKDIR extends FSCmd {
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
  public void exec(final String cmd, final PrintOutput out) throws IOException {
    String dir = "";
    final int beginIndex = path.lastIndexOf('/');
    if(beginIndex == -1) {
      dir = path;
    } else {
      curPre = checkPre(path, FSUtils.goToDir(context.data(), curPre,
          path.substring(0, beginIndex)));

      dir = path.substring(beginIndex + 1);
    }

    if(!FSUtils.validFileName(dir)) error(dir, 101);

    final int src = FSUtils.getDir(context.data(), curPre, dir);
    if(src > -1) error(path, 17);

    // add new dir
    try {
      int preNewFile = 4;
      if(!(curPre == FSUtils.getROOTDIR())) {
        preNewFile = curPre + FSUtils.NUMATT;
      }
      FSUtils.insert(context.data(), true, Token.token(dir), Token.EMPTY,
          Token.ZERO, FSUtils.currTime(), curPre, preNewFile);
    } catch(final Exception e) {
      e.printStackTrace();
    }
  }
}

