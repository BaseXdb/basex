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
  public void exec(final PrintOutput out) throws IOException {
    final ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
    pb.directory(new File(Token.string(fs.path(curPre))));
    
    try {
      final Process pr = pb.start();
      final byte[][] cache = new byte[2][];
      
      // receive input stream
      final Thread t1 = new Thread() {
        @Override
        public void run() { cache[0] = getStream(pr.getInputStream()); }
      };
      // receive error stream
      final Thread t2 = new Thread() {
        @Override
        public void run() { cache[1] = getStream(pr.getErrorStream()); }
      };
      t1.start();
      t2.start();
      t1.join();
      t2.join();
      out.print(cache[cache[1].length == 0 ? 0 : 1]);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      out.println(ex.getMessage());
    }
  }

  /**
   * Returns content of an input stream.
   * @param input stream reference
   * @return content
   */
  byte[] getStream(final InputStream input) {
    try {
      final TokenBuilder tb = new TokenBuilder();
      final byte[] buf = new byte[2048];
      int i = 0;
      while((i = input.read(buf)) != -1) {
        tb.add(i == buf.length ? buf : Array.finish(buf, i));
      }
      return tb.finish();
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return Token.EMPTY;
    }
  }
}
