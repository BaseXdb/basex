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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class ProcFn extends StandardFunc {
  /** QName: result. */
  static final QNm Q_RESULT = new QNm("result");
  /** QName: output. */
  static final QNm Q_OUTPUT = new QNm("output");
  /** QName: error. */
  static final QNm Q_ERROR = new QNm("error");
  /** QName: code. */
  static final QNm Q_CODE = new QNm("code");

  /**
   * Returns the result of a command.
   * @param qc query context
   * @param fork fork process
   * @return result, or {@code null} if process is forked
   * @throws QueryException query exception
   */
  final ProcResult exec(final QueryContext qc, final boolean fork) throws QueryException {
    // arguments
    final String command = toString(arg(0), qc);
    final StringList args = new StringList().add(command);
    final Iter iter = arg(1).iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      args.add(toString(item));
    }

    // options
    final ProcOptions options = toOptions(arg(2), new ProcOptions(), true, qc);
    final String encoding = options.get(ProcOptions.ENCODING);
    final Charset cs;
    try {
      cs = Charset.forName(encoding);
    } catch(final Exception ex) {
      Util.debug(ex);
      throw PROC_ENCODING_X.get(info, encoding);
    }
    final long seconds = options.get(ProcOptions.TIMEOUT);
    final String dir = options.get(ProcOptions.DIR);
    final String input = options.get(ProcOptions.INPUT);

    final ProcResult result = new ProcResult();
    final Process proc;
    final ProcessBuilder pb = new ProcessBuilder(args.finish());
    if(dir != null) pb.directory(toPath(dir).toFile());
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
