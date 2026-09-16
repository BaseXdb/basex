package org.basex.query.func.proc;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.lang.ProcessBuilder.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Process function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ProcFn extends StandardFunc {
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
    for(final Item item : arg(1).atomValue(qc, info)) args.add(toString(item));

    // options
    final ProcOptions options = toOptions(arg(2), new ProcOptions(), qc);
    final String encoding = toEncodingOrNull(options.get(ProcOptions.ENCODING), PROC_ENCODING_X);
    final byte[] input = input(options.get(ProcOptions.INPUT), encoding);
    final long seconds = options.get(ProcOptions.TIMEOUT);
    final String dir = options.get(ProcOptions.DIR);
    final Map<String, String> env = options.get(ProcOptions.ENVIRONMENT).free();

    final ProcResult result = new ProcResult(options, encoding);
    final Process proc;
    final ProcessBuilder pb = new ProcessBuilder(args.finish());
    if(dir != null) pb.directory(toPath(dir, qc).toFile());
    if(!env.isEmpty()) {
      pb.environment().clear();
      pb.environment().putAll(env);
    }
    // output of a forked process is not consumed
    if(fork) pb.redirectOutput(Redirect.DISCARD).redirectError(Redirect.DISCARD);

    try {
      proc = pb.start();
    } catch(final IOException ex) {
      result.exception(ex);
      return result;
    }

    // standard input is closed in any case, so that processes waiting for input terminate
    final Thread writer = new Thread(() -> {
      try(OutputStream os = proc.getOutputStream()) {
        if(input != null) os.write(input);
      } catch(final IOException ex) {
        // the process may terminate without consuming its input
        Util.debug(ex);
      }
    });
    writer.start();
    if(fork) return null;

    final Thread outt = reader(proc.getInputStream(), result.output, result);
    final Thread errt = reader(proc.getErrorStream(), result.error, result);
    outt.start();
    errt.start();

    final Thread thread = new Thread(() -> {
      try {
        proc.waitFor();
        writer.join();
        outt.join();
        errt.join();
      } catch(final InterruptedException ex) {
        Util.debug(ex);
      }
    });
    thread.start();

    final Performance perf = new Performance();
    try {
      while(thread.isAlive()) {
        qc.checkStop();
        if(seconds > 0 && perf.nanoRuntime(false) / 1000000000 >= seconds) {
          proc.destroyForcibly();
          thread.interrupt();
          throw PROC_TIMEOUT.get(info);
        }
        Performance.sleep(10);
      }
      result.code = proc.exitValue();
      return result;
    } catch(final JobException ex) {
      proc.destroyForcibly();
      thread.interrupt();
      throw ex;
    }
  }

  /**
   * Returns the output of a process.
   * @param result process result
   * @return string or binary item
   * @throws QueryException query exception
   */
  final Item output(final ProcResult result) throws QueryException {
    final byte[] output = result.output.finish();
    return result.options.get(ProcOptions.BINARY) ? B64.get(output) :
      Str.get(decode(output, result, result.options.get(ProcOptions.FALLBACK)));
  }

  /**
   * Returns the error output of a process; invalid characters are replaced.
   * @param result process result
   * @return error output
   * @throws QueryException query exception
   */
  final byte[] error(final ProcResult result) throws QueryException {
    return decode(result.error.finish(), result, true);
  }

  /**
   * Converts the input to bytes.
   * @param input input (string or binary item, or empty sequence)
   * @param encoding encoding (can be {@code null})
   * @return bytes or {@code null}
   * @throws QueryException query exception
   */
  private byte[] input(final Value input, final String encoding) throws QueryException {
    if(input.isEmpty()) return null;
    if(input instanceof final Bin bin) return bin.binary(info);
    try {
      return ConvertFn.toBinary(((Item) input).string(info), encoding);
    } catch(final IOException ex) {
      throw PROC_ENCODING_X.get(info, ex);
    }
  }

  /**
   * Decodes process output.
   * @param bytes bytes
   * @param result process result
   * @param fallback replace invalid characters
   * @return decoded string
   * @throws QueryException query exception
   */
  private byte[] decode(final byte[] bytes, final ProcResult result, final boolean fallback)
      throws QueryException {
    final ArrayInput ai = new ArrayInput(bytes);
    try(TextInput ti = result.options.get(ProcOptions.NORMALIZE_NEWLINES) ?
      new NewlineInput(ai, result.encoding) : new TextInput(ai, result.encoding)) {
      return ti.fallback(fallback).content();
    } catch(final IOException ex) {
      throw PROC_ENCODING_X.get(info, ex);
    }
  }

  /**
   * Creates a reader thread.
   * @param in input stream
   * @param out output stream
   * @param result process result
   * @return result
   */
  private static Thread reader(final InputStream in, final ArrayOutput out,
      final ProcResult result) {
    return new Thread(() -> {
      try(in) {
        in.transferTo(out);
      } catch(final IOException ex) {
        result.exception(ex);
      }
    });
  }
}
