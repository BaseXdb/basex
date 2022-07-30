package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class remembers descriptive query information sent back to the client.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class QueryInfo {
  /** Maximum size for compilation and evaluation output. */
  private static final int MAX = 1 << 20;
  /** Maximum size for compilation and evaluation output per line. */
  private static final int MAX_LINE = 1 << 14;

  /** Parsing time (nano seconds). */
  public final AtomicLong parsing = new AtomicLong();
  /** Compilation time (nano seconds). */
  public final AtomicLong compiling = new AtomicLong();
  /** Optimization time (nano seconds). */
  public final AtomicLong optimizing = new AtomicLong();
  /** Evaluation time (nano seconds). */
  public final AtomicLong evaluating = new AtomicLong();
  /** Serialization time (nano seconds). */
  public final AtomicLong serializing = new AtomicLong();

  /** Compilation info. */
  private final TokenBuilder compile = new TokenBuilder();
  /** Optimization info. */
  private final TokenBuilder optimize = new TokenBuilder();
  /** Evaluation info. */
  private final TokenBuilder evaluate = new TokenBuilder();

  /** Verbose info. */
  private final boolean queryinfo;
  /** Number of runs. */
  private final int runs;

  /** Runtime flag. */
  boolean runtime;
  /** Query string. */
  String query;

  /**
   * Constructor.
   * @param context database context
   */
  public QueryInfo(final Context context) {
    final MainOptions mopts = context.options;
    queryinfo = mopts.get(MainOptions.QUERYINFO);
    runs = Math.max(1, mopts.get(MainOptions.RUNS));
  }

  /**
   * Resets info strings.
   */
  public void reset() {
    compile.reset();
    optimize.reset();
    evaluate.reset();
  }

  /**
   * Adds some compilation info.
   * @param dynamic dynamic compilation
   * @param string evaluation info
   * @param ext text text extensions
   */
  void compInfo(final boolean dynamic, final String string, final Object... ext) {
    final TokenBuilder tb = dynamic ? optimize : compile;
    if(queryinfo && tb.size() < MAX) {
      final TokenList list = new TokenList(ext.length);
      for(final Object e : ext) list.add(QueryError.normalize(e, null));
      String info = Util.info(string, (Object[]) list.finish());
      if(!info.isEmpty()) {
        if(runtime) {
          info = "RUNTIME: " + info;
          if(Prop.debug) Util.stack(info);
        }
        tb.add(LI).add(info).add(NL);
        if(tb.size() >= MAX) tb.add(LI).add(DOTS).add(NL);
      }
    }
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  void evalInfo(final String string) {
    if(queryinfo) {
      synchronized(evaluate) {
        if(evaluate.size() < MAX) {
          evaluate.add(LI).add(chop(token(string.replaceAll("\r?\n", "|")), MAX_LINE)).add(NL);
          if(evaluate.size() >= MAX) evaluate.add(LI).add(DOTS).add(NL);
        }
      }
    }
  }

  /**
   * Returns detailed query information.
   * @param qp query processor
   * @param printed printed bytes
   * @param hits number of returned hits
   * @param locks read and write locks
   * @param success success flag
   * @return query string
   */
  public String toString(final QueryProcessor qp, final long printed, final long hits,
      final Locks locks, final boolean success) {

    final TokenBuilder tb = new TokenBuilder();
    final String total = Performance.getTime(parsing.get() + compiling.get() + optimizing.get() +
        evaluating.get() + serializing.get(), runs);
    if(queryinfo) {
      tb.add(NL);
      if(query != null) {
        tb.add(QUERY).add(COL).add(NL);
        tb.add(QueryParser.removeComments(query, Integer.MAX_VALUE)).add(NL).add(NL);
      }
      if(!compile.isEmpty()) {
        tb.add(COMPILING).add(COL).add(NL);
        tb.add(compile).add(NL);
      }
      if(!optimize.isEmpty()) {
        tb.add(OPTIMIZING).add(COL).add(NL);
        tb.add(optimize).add(NL);
      }
      tb.add(OPTIMIZED_QUERY).add(COL).add(NL);
      tb.add(qp.qc.main == null ? qp.qc.functions : usedDecls(qp.qc.main)).add(NL);
      tb.add(NL);
      if(!evaluate.isEmpty()) {
        tb.add(EVALUATING).add(COL).add(NL);
        tb.add(evaluate).add(NL);
      }
      tb.add(PARSING_CC).add(Performance.getTime(parsing.get(), runs)).add(NL);
      tb.add(COMPILING_CC).add(Performance.getTime(compiling.get(), runs)).add(NL);
      tb.add(OPTIMIZING_CC).add(Performance.getTime(optimizing.get(), runs)).add(NL);
      tb.add(EVALUATING_CC).add(Performance.getTime(evaluating.get(), runs)).add(NL);
      tb.add(PRINTING_CC).add(Performance.getTime(serializing.get(), runs)).add(NL);
      tb.add(TOTAL_TIME_CC).add(total).add(NL).add(NL);
      tb.add(NUMBER_CC + hits).add(' ').add(hits == 1 ? ITEM : ITEMS).add(NL);
      final int up = qp.updates();
      tb.add(UPDATED_CC + up).add(' ').add(up == 1 ? ITEM : ITEMS).add(NL);
      tb.add(PRINTED_CC).add(Performance.format(printed)).add(NL);
      if(locks != null) {
        tb.add(READ_LOCKING_CC).add(locks.reads).add(NL);
        tb.add(WRITE_LOCKING_CC).add(locks.writes).add(NL);
      }
    }
    if(success) {
      final IO baseIO = qp.sc.baseIO();
      final String name = baseIO == null ? "" : " \"" + baseIO.name() + '"';
      tb.add(NL).addExt(QUERY_EXECUTED_X_X, name, total);
    }
    return tb.toString();
  }

  /**
   * Serializes all functions and variables reachable from the given main module.
   * @param module module to start from
   * @return the string representation
   */
  static String usedDecls(final MainModule module) {
    final IdentityHashMap<Scope, Object> map = new IdentityHashMap<>();
    final StringBuilder sb = new StringBuilder();
    module.visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        if(map.put(var, var) == null) {
          var.visit(this);
          sb.append(var).append(NL);
        }
        return true;
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        final StaticFunc f = call.func();
        if(map.put(f, f) == null) {
          f.visit(this);
          sb.append(f).append(NL);
        }
        return true;
      }

      @Override
      public boolean inlineFunc(final Scope scope) {
        if(map.put(scope, scope) == null) scope.visit(this);
        return true;
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        if(map.put(func, func) == null) func.visit(this);
        return true;
      }
    });
    return sb.append(module).toString();
  }
}
