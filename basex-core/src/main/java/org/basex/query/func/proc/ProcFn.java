package org.basex.query.func.proc;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Process function.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class ProcFn extends StandardFunc {
  /** Name: result. */
  static final String RESULT = "result";
  /** Name: standard output. */
  static final String OUTPUT = "output";
  /** Name: standard error. */
  static final String ERROR = "error";
  /** Name: code. */
  static final String CODE = "code";

  /**
   * Returns the result of a command.
   * @param qc query context
   * @param fork fork process
   * @return result, or {@code null} if process is forked
   * @throws QueryException query exception
   */
  final Result exec(final QueryContext qc, final boolean fork) throws QueryException {
    checkAdmin(qc);

    // arguments
    final TokenList tl = new TokenList();
    tl.add(toToken(exprs[0], qc));
    if(exprs.length > 1) {
      final Iter iter = qc.iter(exprs[1]);
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        tl.add(toToken(it));
      }
    }
    final String[] args = tl.toStringArray();

    // options
    final ProcOptions opts = toOptions(2, new ProcOptions(), qc);
    final String enc = opts.get(ProcOptions.ENCODING);
    final Charset cs;
    try {
      cs = Charset.forName(enc);
    } catch(final Exception ex) {
      Util.debug(ex);
      throw PROC_ENCODING_X.get(info, enc);
    }
    final long sec = opts.get(ProcOptions.TIMEOUT);
    final String dir = opts.get(ProcOptions.DIR);

    final Result result = new Result();
    final Process proc;
    final ProcessBuilder pb = new ProcessBuilder(args);
    if(dir != null) pb.directory(toPath(token(dir)).toFile());
    try {
      proc = pb.start();
    } catch(final IOException ex) {
      result.error.add(token(Util.message(ex)));
      result.code = 9999;
      return result;
    }
    if(fork) return null;

    final Thread outt = reader(proc.getInputStream(), result.output, cs);
    final Thread errt = reader(proc.getErrorStream(), result.error, cs);
    outt.start();
    errt.start();

    final Thread thread = new Thread(() -> {
      try {
        proc.waitFor();
        outt.join();
        errt.join();
      } catch(final InterruptedException ex) {
        result.error.add(token(Util.message(ex)));
      }
    });
    thread.start();

    final Performance perf = new Performance();
    try {
      while(thread.isAlive()) {
        qc.checkStop();
        if(sec > 0 && (System.nanoTime() - perf.start()) / 1000000000 >= sec) {
          thread.interrupt();
          throw PROC_TIMEOUT.get(info);
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
   * @param tb token builder
   * @param cs charset
   * @return result
   */
  private static Thread reader(final InputStream in, final TokenBuilder tb, final Charset cs) {
    final InputStreamReader isr = new InputStreamReader(in, cs);
    final BufferedReader br = new BufferedReader(isr);
    return new Thread(() -> {
      try {
        for(int b; (b = br.read()) != -1;) tb.add(b);
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    });
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
