package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions to execute system commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNProc extends StandardFunc {
  /** Name: result. */
  private static final String RESULT = "result";
  /** Name: standard output. */
  private static final String OUTPUT = "output";
  /** Name: standard error. */
  private static final String ERROR = "error";
  /** Name: code. */
  private static final String CODE = "code";

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNProc(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final TokenList tl = new TokenList();
    tl.add(checkStr(exprs[0], qc));
    if(exprs.length > 1) {
      final Iter ir = qc.iter(exprs[1]);
      for(Item it; (it = ir.next()) != null;) tl.add(checkStr(it));
    }

    final String c = exprs.length > 2 ? string(checkStr(exprs[2], qc)) : Prop.ENCODING;
    final Charset cs;
    try {
      cs = Charset.forName(c);
    } catch(final Exception ex) {
      throw BXPR_ENC.get(info, c);
    }

    final String[] args = tl.toStringArray();
    switch(func) {
      case _PROC_SYSTEM:  return system(args, cs);
      case _PROC_EXECUTE: return execute(args, cs);
      default:            return super.item(qc, ii);
    }
  }

  /**
   * Executes the specified command and returns the result as string.
   * @param args command and arguments
   * @param cs charset
   * @return result
   * @throws QueryException query exception
   */
  private Str system(final String[] args, final Charset cs) throws QueryException {
    final Result result = exec(args, cs);
    if(result.code == 0) return Str.get(norm(result.output));
    // create error message
    final QNm name = new QNm("PROC" + String.format("%04d", result.code));
    throw new QueryException(info, name, string(norm(result.error)));
  }

  /**
   * Executes the specified command and returns the result as element.
   * @param args command and arguments
   * @param cs charset
   * @return result
   */
  private static FElem execute(final String[] args, final Charset cs) {
    final Result result = exec(args, cs);
    final FElem root = new FElem(RESULT);
    root.add(new FElem(OUTPUT).add(norm(result.output)));
    root.add(new FElem(ERROR).add(norm(result.error)));
    root.add(new FElem(CODE).add(token(result.code)));
    return root;
  }

  /**
   * Returns the result of a command.
   * @param args command and arguments
   * @param cs charset
   * @return result
   */
  private static Result exec(final String[] args, final Charset cs) {
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
  private static Thread reader(final InputStream in, final TokenBuilder tb,
      final Charset cs) {
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
  private static byte[] norm(final TokenBuilder tb) {
    return delete(tb.finish(), '\r');
  }

  /**
   * Error object.
   */
  static class Result {
    /** Process output. */
    final TokenBuilder output = new TokenBuilder();
    /** Process error. */
    final TokenBuilder error = new TokenBuilder();
    /** Exit code. */
    int code;
  }
}
