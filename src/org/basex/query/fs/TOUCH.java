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
public final class TOUCH extends FSCmd {
  /** Specified path. */
  private String path;

  @Override
  public void args(final String args, final int pos) throws FSException {
    // check default options and get path
    path = defaultOpts(args, pos).getPath();
    // no file/path was specified...
    if(path == null) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    final int beginIndex = path.lastIndexOf('/');
    if(beginIndex > -1) {
      curPre = checkPre(path, FSUtils.goToDir(context.data(), curPre,
          path.substring(0, beginIndex)));
    }

    final String file = path.substring(path.lastIndexOf('/') + 1);

    final int[] preFound =  FSUtils.getChildren(context.data(),
        curPre, file);

    if(preFound.length > 0) {
      for(final int i : preFound) {
        if(FSUtils.isFile(context.data(), i)) {
          FSUtils.setMtime(data, i, FSUtils.currTime());
        }
      }
    } else {
      // add new file
      if(!FSUtils.validFileName(file)) error(file, 101);

      try {
        int preNewFile = 4;
        if(!(curPre == FSUtils.getROOTDIR())) {
          preNewFile = curPre + FSUtils.NUMATT;
        }
        FSUtils.insert(context.data(), false, Token.token(file),
            getSuffix(file), Token.ZERO, FSUtils.currTime(),
            curPre, preNewFile);
      } catch(final Exception e) {
        e.printStackTrace();
      }
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