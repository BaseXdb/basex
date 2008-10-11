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
public final class Mkdir extends FSCmd {
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
    String dir = path;
    if(i != -1) {
      curPre(path.substring(0, i));
      dir = path.substring(i + 1);
    }

    // valid file name?
    if(!fs.valid(dir)) error(dir, 101);
    // dir exists already?
    if(fs.dir(curPre, dir) > -1) error(path, 17);

    // add new dir
    final int pn = curPre == DataFS.ROOTDIR ? 4 : curPre + DataFS.NUMATT;
    fs.insert(true, Token.token(dir), Token.EMPTY, Token.ZERO,
        fs.currTime(), curPre, pn);
  }
}

