package org.basex.query.func.proc;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.core.jobs.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Process function.
 *
 * @author BaseX Team 2005-16, BSD License
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
    checkAdmin(qc);

    // arguments
    final TokenList tl = new TokenList();
    tl.add(toToken(exprs[0], qc));
    if(exprs.length > 1) {
      final Iter ir = qc.iter(exprs[1]);
      for(Item it; (it = ir.next()) != null;) tl.add(toToken(it));
    }
    final String[] args = tl.toStringArray();

    // encoding
    final ProcOptions opts = new ProcOptions();
    if(exprs.length > 2) {
      // backward compatibility...
      final Item item = exprs[2].item(qc, info);
      if(item != null && item.type.isStringOrUntyped()) {
        opts.set(ProcOptions.ENCODING, string(toEmptyToken(exprs[2], qc)));
      } else {
        toOptions(2, opts, qc);
      }
    }
    final String encoding = opts.get(ProcOptions.ENCODING);
    final Charset cs;
    try {
      cs = Charset.forName(encoding);
    } catch(final Exception ex) {
      throw BXPR_ENC_X.get(info, encoding);
    }

    // options
    final long sec = opts.get(ProcOptions.TIMEOUT);

    final Result result = new Result();
    final Process proc;
    try {
      final ProcessBuilder pb = new ProcessBuilder(args);
      final String dir = opts.get(ProcOptions.DIR);
      if(dir != null) pb.directory(toPath(token(dir)).toFile());
      proc = pb.start();
    } catch(final IOException ex) {
      try {
        result.error.write(token(Util.message(ex)));
      } catch(final IOException ignore) { }
      result.code = 9999;
      return result;
    }

    final Thread outt = reader(proc.getInputStream(), result.output, cs);
    final Thread errt = reader(proc.getErrorStream(), result.error, cs);
    outt.start();
    errt.start();

    final Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          proc.waitFor();
          outt.join();
          errt.join();
        } catch(final InterruptedException ex) {
          try {
            result.error.write(token(Util.message(ex)));
          } catch(final IOException ignored) { }
        }
      }
    };
    thread.start();

    final Performance perf = new Performance();
    try {
      while(thread.isAlive()) {
        qc.checkStop();
        if(sec > 0 && (System.nanoTime() - perf.start()) / 1000000000 >= sec) {
          thread.interrupt();
          throw BXPR_TIMEOUT.get(info);
        }
        Performance.sleep(10);
      }
      result.code = proc.exitValue();
      return result;
    } catch(final JobException ex) {
      Util.debug(ex);
      thread.interrupt();
      throw ex;
    }
  }

  /**
   * Creates a reader thread.
   * @param in input stream
   * @param ao cache
   * @param cs charset
   * @return result
   */
  private static Thread reader(final InputStream in, final ArrayOutput ao, final Charset cs) {
    final InputStreamReader isr = new InputStreamReader(in, cs);
    final BufferedReader br = new BufferedReader(isr);
    return new Thread() {
      @Override
      public void run() {
        try {
          for(int b; (b = br.read()) != -1;) ao.write(b);
        } catch(final IOException ex) {
          Util.stack(ex);
        }
      }
    };
  }

  /**
   * Error object.
   */
  static final class Result {
    /** Process output. */
    final ArrayOutput output = new ArrayOutput();
    /** Process error. */
    final ArrayOutput error = new ArrayOutput();
    /** Exit code. */
    int code;
  }
}
