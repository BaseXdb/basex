package org.basex.fs;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * Performs a pwd command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Pwd extends FSCmd {
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args) throws FSException {
    // check default options and get path
    path = defaultOpts(args).getPath();
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    // if there is path expression go to dir
    if(path != null) curPre(path);
    out.println(fs.path(curPre));
  }
}
