package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class remembers descriptive query information sent back to the client.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class QueryInfo {
  /** Verbose info. */
  private final boolean verbose;

  /** Read locked databases. */
  public StringList readLocked;
  /** Write locked databases. */
  public StringList writeLocked;
  /** Parsing time. */
  public long parsing;
  /** Compilation time. */
  public long compiling;
  /** Evaluation time. */
  public long evaluating;
  /** Serialization time. */
  public long serializing;
  /** Query. */
  public String query;

  /** Runtime flag. */
  boolean runtime;
  /** Compilation info. */
  private final TokenList compile = new TokenList(0);
  /** Evaluation info. */
  private final TokenList evaluate = new TokenList(0);

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
    if(verbose) {
      String info = Util.info(string,  ext);
      if(runtime) {
        info = "RUNTIME: " + info;
        if(Prop.debug) Util.stack(info);
      }
      compile.add(info);
    }
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final String string) {
    if(verbose) evaluate.add(token(string.replaceAll("\r?\n\\s*", " ")));
  }

  /**
   * Returns detailed query information.
   * @param qp query processor
   * @param printed printed bytes
   * @param hits number of returned hits
   * @param detailed return detailed query info
   * @return query string
   */
  public String toString(final QueryProcessor qp, final long printed, final long hits,
      final boolean detailed) {

    final int runs = Math.max(1, qp.ctx.context.options.get(MainOptions.RUNS));
    final TokenBuilder tb = new TokenBuilder();
    final long total = parsing + compiling + evaluating + serializing;
    if(detailed) {
      final int up = qp.updates();
      tb.add(toString(qp.ctx)).add(NL);
      tb.add(PARSING_CC).add(Performance.getTime(parsing, runs)).add(NL);
      tb.add(COMPILING_CC).add(Performance.getTime(compiling, runs)).add(NL);
      tb.add(EVALUATING_CC).add(Performance.getTime(evaluating, runs)).add(NL);
      tb.add(PRINTING_CC).add(Performance.getTime(serializing, runs)).add(NL);
      tb.add(TOTAL_TIME_CC).add(Performance.getTime(total, runs)).add(NL).add(NL);
      tb.add(HITS_X_CC + hits).add(' ').add(hits == 1 ? ITEM : ITEMS).add(NL);
      tb.add(UPDATED_CC + up).add(' ').add(up == 1 ? ITEM : ITEMS).add(NL);
      tb.add(PRINTED_CC).add(Performance.format(printed)).add(NL);
      tb.add(READ_LOCKING_CC);
      if(readLocked == null) tb.add(GLOBAL);
      else if(readLocked.isEmpty()) tb.add(NONE);
      else tb.add(LOCAL).add(' ').add(Arrays.toString(readLocked.toArray()));
      tb.add(NL).add(WRITE_LOCKING_CC);
      if(writeLocked == null) tb.add(GLOBAL);
      else if(writeLocked.isEmpty()) tb.add(NONE);
      else tb.add(LOCAL).add(' ').add(Arrays.toString(writeLocked.toArray()));
      tb.add(NL);
    }
    final IO io = qp.sc.baseIO();
    final String name = io == null ? "" : " \"" + io.name() + '"';
    tb.addExt(NL + QUERY_EXECUTED_X_X, name, Performance.getTime(total, runs));
    return tb.toString();
  }

  /**
   * Returns detailed compilation and evaluation information.
   * @param qc query context
   * @return string
   */
  public String toString(final QueryContext qc) {
    final TokenBuilder tb = new TokenBuilder();
    if(query != null) {
      final String qu = QueryProcessor.removeComments(query, Integer.MAX_VALUE);
      tb.add(NL).add(QUERY).add(COL).add(NL).add(qu).add(NL);
    }
    if(!compile.isEmpty()) {
      tb.add(NL).add(COMPILING).add(COL).add(NL);
      for(final byte[] line : compile) tb.add(LI).add(line).add(NL);
      tb.add(NL).add(OPTIMIZED_QUERY).add(COL).add(NL);
      tb.add(qc.root == null ? qc.funcs.toString() : usedDecls(qc.root)).add(NL);
    }
    if(!evaluate.isEmpty()) {
      tb.add(NL).add(EVALUATING).add(COL).add(NL);
      for(final byte[] line : evaluate) tb.add(LI).add(line).add(NL);
    }
    return tb.toString();
  }

  /**
   * Serializes all functions and variables reachable from the given main module.
   * @param mod module to start from
   * @return the string representation
   */
  private String usedDecls(final MainModule mod) {
    final IdentityHashMap<Scope, Object> map = new IdentityHashMap<Scope, Object>();
    final StringBuilder sb = new StringBuilder();
    mod.visit(new ASTVisitor() {
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
      public boolean inlineFunc(final Scope sub) {
        if(map.put(sub, sub) == null) sub.visit(this);
        return true;
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        if(map.put(func, func) == null) func.visit(this);
        return true;
      }
    });
    return sb.append(mod).toString();
  }
}
