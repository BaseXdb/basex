package org.basex.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.basex.BaseX;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Performs an external command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Ext extends FSCmd {
  /** Specified command. */
  private String cmd;

  @Override
  public void args(final String args) {
    cmd = args;
  }

  @Override
  public void exec(final PrintOutput out) throws FSException {
    // build process... splitting by spaces might be too simple here
    final ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
    pb.directory(new File(Token.string(fs.path(curPre))));
    final byte[][] cache = new byte[2][];
    
    try {
      // catch standard and error output
      final Process pr = pb.start();
      final Thread t1 = getThread(pr.getInputStream(), cache, 0);
      final Thread t2 = getThread(pr.getErrorStream(), cache, 1);
      t1.start();
      t2.start();
      t1.join();
      t2.join();
      
      // successful execution
      if(cache[1].length == 0) out.print(cache[0]);
      // error occurred
      else throw new Exception(Token.string(Token.trim(cache[1])));
      
    } catch(final Exception ex) {
      // catch other exceptions (file not found, stream interrupted, ...)
      BaseX.debug(ex);
      // throw simplified error message
      throw new FSException(ex.getMessage().replaceAll(": java.io.*", ""));
    }
  }

  /**
   * Returns content of an input stream.
   * @param is stream reference
   * @param c output cache
   * @param n cache number
   * @return content
   */
  private Thread getThread(final InputStream is, final byte[][] c,
      final int n) {
    return new Thread() {
      @Override
      public void run() {
        final TokenBuilder tb = new TokenBuilder();
        final byte[] buf = new byte[2048];
        try {
          int i = 0;
          while((i = is.read(buf)) != -1) {
            tb.add(i == buf.length ? buf : Array.finish(buf, i));
          }
        } catch(final IOException ex) {
          BaseX.debug(ex);
        }
        c[n] = tb.finish();
      }
    };
  }
}
