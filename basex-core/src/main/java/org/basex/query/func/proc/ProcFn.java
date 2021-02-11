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
 * @author BaseX Team 2005-21, BSD License
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
  final ProcResult exec(final QueryContext qc, final boolean fork) throws QueryException {
    checkAdmin(qc);

    // arguments
    final StringList sl = new StringList();
    sl.add(toToken(exprs[0], qc));
    if(exprs.length > 1) {
      final Iter iter = exprs[1].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) sl.add(toToken(item));
    }
    final String[] args = sl.finish();

    // options
    final ProcOptions opts = toOptions(2, new ProcOptions(), qc);
    final String encoding = opts.get(ProcOptions.ENCODING);
    final Charset cs;
    try {
      cs = Charset.forName(encoding);
    } catch(final Exception ex) {
      Util.debug(ex);
      throw PROC_ENCODING_X.get(info, encoding);
    }
    final long seconds = opts.get(ProcOptions.TIMEOUT);
    final String dir = opts.get(ProcOptions.DIR);
    final String input = opts.get(ProcOptions.INPUT);

    final ProcResult result = new ProcResult();
    final Process proc;
    final ProcessBuilder pb = new ProcessBuilder(args);
    if(dir != null) pb.directory(toPath(token(dir)).toFile());
    try {
      proc = pb.start();
    } catch(final IOException ex) {
      result.exception(ex);
      return result;
    }
    if(fork) return null;

    final Thread outt = reader(proc.getInputStream(), result.output, cs, result);
    final Thread errt = reader(proc.getErrorStream(), result.error, cs, result);
    outt.start();
    errt.start();

    final Thread thread = new Thread(() -> {
      try {
        if(input != null) {
          try(OutputStream os = proc.getOutputStream()) {
            os.write(token(input));
          }
        }
        proc.waitFor();
        outt.join();
        errt.join();
      } catch(final IOException ex) {
        result.exception(ex);
      } catch(final InterruptedException ex) {
        result.error.add(Util.message(ex));
      }
    });
    thread.start();

    final Performance perf = new Performance();
    try {
      while(thread.isAlive()) {
        qc.checkStop();
        if(seconds > 0 && perf.ns(false) / 1000000000 >= seconds) {
          thread.interrupt();
          throw PROC_TIMEOUT.get(info);
        }
        Performance.sleep(10);
      }
      result.code = proc.exitValue();
      return result;
    } catch(final JobException ex) {
      thread.interrupt();
      throw ex;
    }
  }

  /**
   * Creates a reader thread.
   * @param in input stream
   * @param tb token builder
   * @param cs charset
   * @param pr process result
   * @return result
   */
  private static Thread reader(final InputStream in, final TokenBuilder tb, final Charset cs,
      final ProcResult pr) {
    final InputStreamReader isr = new InputStreamReader(in, cs);
    final BufferedReader br = new BufferedReader(isr);
    return new Thread(() -> {
      try {
        for(int b; (b = br.read()) != -1;) tb.add(b);
      } catch(final IOException ex) {
        pr.exception = ex;
      }
    });
  }
}
