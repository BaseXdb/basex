package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

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

  /** Verbose info. */
  private final boolean verbose;

  /** Parsing time (nano seconds). */
  public long parsing;
  /** Compilation time (nano seconds). */
  public long compiling;
  /** Evaluation time (nano seconds). */
  public long evaluating;
  /** Serialization time (nano seconds). */
  public long serializing;

  /** Query. */
  String query;
  /** Runtime flag. */
  boolean runtime;
  /** Compilation info. */
  private final TokenBuilder compile = new TokenBuilder();
  /** Evaluation info. */
  private final TokenBuilder evaluate = new TokenBuilder();

  /**
   * Constructor.
   * @param qc query context
   */
  QueryInfo(final QueryContext qc) {
    verbose = qc.context.options.get(MainOptions.QUERYINFO) || Prop.debug;
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  void compInfo(final String string, final Object... ext) {
    if(verbose && compile.size() < MAX) {
      final TokenList list = new TokenList(ext.length);
      for(final Object e : ext) {
        list.add(e instanceof Supplier<?> ? token(((Supplier<?>) e).get().toString()) :
          QueryError.normalize(e, null));
      }
      String info = Util.info(string, (Object[]) list.finish());
      if(!info.isEmpty()) {
        if(runtime) {
          info = "RUNTIME: " + info;
          if(Prop.debug) Util.stack(info);
        }
        compile.add(LI).add(info).add(NL);
        if(compile.size() >= MAX) compile.add(LI).add(DOTS).add(NL);
      }
    }
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  void evalInfo(final String string) {
    if(verbose) {
      synchronized(evaluate) {
        if(evaluate.size() < MAX) {
          evaluate.add(LI).add(chop(token(string.replaceAll("\r?\n", "|")), MAX_LINE)).add(NL);
          if(evaluate.size() >= MAX) compile.add(LI).add(DOTS).add(NL);
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
   * @return query string
   */
  public String toString(final QueryProcessor qp, final long printed, final long hits,
      final Locks locks) {
    final int runs = Math.max(1, qp.qc.context.options.get(MainOptions.RUNS));
    final TokenBuilder tb = new TokenBuilder();
    final long total = parsing + compiling + evaluating + serializing;
    if(qp.qc.context.options.get(MainOptions.QUERYINFO)) {
      final int up = qp.updates();
      tb.add(toString(qp.qc)).add(NL);
      tb.add(PARSING_CC).add(Performance.getTime(parsing, runs)).add(NL);
      tb.add(COMPILING_CC).add(Performance.getTime(compiling, runs)).add(NL);
      tb.add(EVALUATING_CC).add(Performance.getTime(evaluating, runs)).add(NL);
      tb.add(PRINTING_CC).add(Performance.getTime(serializing, runs)).add(NL);
      tb.add(TOTAL_TIME_CC).add(Performance.getTime(total, runs)).add(NL).add(NL);
      tb.add(HITS_X_CC + hits).add(' ').add(hits == 1 ? ITEM : ITEMS).add(NL);
      tb.add(UPDATED_CC + up).add(' ').add(up == 1 ? ITEM : ITEMS).add(NL);
      tb.add(PRINTED_CC).add(Performance.format(printed)).add(NL);
      if(locks != null) {
        tb.add(READ_LOCKING_CC).add(locks.reads).add(NL);
        tb.add(WRITE_LOCKING_CC).add(locks.writes).add(NL);
      }
    }
    final IO baseIO = qp.sc.baseIO();
    final String name = baseIO == null ? "" : " \"" + baseIO.name() + '"';
    tb.addExt(NL + QUERY_EXECUTED_X_X, name, Performance.getTime(total, runs));
    return tb.toString();
  }

  /**
   * Returns detailed compilation and evaluation information.
   * @param qc query context
   * @return string
   */
  String toString(final QueryContext qc) {
    final TokenBuilder tb = new TokenBuilder();
    if(query != null) {
      final String string = QueryParser.removeComments(query, Integer.MAX_VALUE);
      tb.add(NL).add(QUERY).add(COL).add(NL).add(string).add(NL);
    }
    if(!compile.isEmpty()) tb.add(NL).add(COMPILING).add(COL).add(NL).add(compile);
    tb.add(NL).add(OPTIMIZED_QUERY).add(COL).add(NL);
    tb.add(qc.root == null ? qc.funcs : usedDecls(qc.root)).add(NL);
    if(!evaluate.isEmpty()) tb.add(NL).add(EVALUATING).add(COL).add(NL).add(evaluate);
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
