package org.basex.query.func.proc;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Process function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class ProcFn extends StandardFunc {
  /**
   * Returns the result of a command.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  final Result exec(final QueryContext qc) throws QueryException {
    checkCreate(qc);

    // arguments
    final TokenList tl = new TokenList();
    tl.add(toToken(exprs[0], qc));
    if(exprs.length > 1) {
      final Iter ir = qc.iter(exprs[1]);
      for(Item it; (it = ir.next()) != null;) tl.add(toToken(it));
    }
    final String[] args = tl.toStringArray();

    // encoding
    final String c = exprs.length > 2 ? string(toToken(exprs[2], qc)) : Prop.ENCODING;
    final Charset cs;
    try {
      cs = Charset.forName(c);
    } catch(final Exception ex) {
      throw BXPR_ENC_X.get(info, c);
    }

    final Result result = new Result();
    final Process proc;
    try {
      proc = new ProcessBuilder(args).start();
    } catch(final IOException ex) {
      result.error.add(Util.message(ex));
      result.code = 9999;
      return result;
    }

    try {
      final Thread outt = reader(proc.getInputStream(), result.output, cs);
      final Thread errt = reader(proc.getErrorStream(), result.error, cs);
      outt.start();
      errt.start();
      proc.waitFor();
      outt.join();
      errt.join();
    } catch(final InterruptedException ex) {
      result.error.add(Util.message(ex));
    }
    result.code = proc.exitValue();
    return result;
  }

  /**
   * Creates a reader thread.
   * @param in input stream
   * @param tb cache
   * @param cs charset
   * @return result
   */
  private static Thread reader(final InputStream in, final TokenBuilder tb, final Charset cs) {
    final InputStreamReader isr = new InputStreamReader(in, cs);
    final BufferedReader br = new BufferedReader(isr);
    return new Thread() {
      @Override
      public void run() {
        try {
          for(int b; (b = br.read()) != -1;) tb.add(b);
        } catch(final IOException ex) {
          Util.stack(ex);
        }
      }
    };
  }

  /**
   * Returns a normalized token from the specified builder.
   * @param tb token builder
   * @return output
   */
  static byte[] norm(final TokenBuilder tb) {
    return delete(tb.toArray(), '\r');
  }

  /**
   * Error object.
   */
  static final class Result {
    /** Process output. */
    final TokenBuilder output = new TokenBuilder();
    /** Process error. */
    final TokenBuilder error = new TokenBuilder();
    /** Exit code. */
    int code;
  }
}
