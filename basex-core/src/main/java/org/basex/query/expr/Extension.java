package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Pragma extension.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragmas of the extension expression. */
  private final Pragma[] pragmas;

  /**
   * Constructor.
   * @param info input info
   * @param pragmas pragmas
   * @param expr enclosed expression
   */
  public Extension(final InputInfo info, final Pragma[] pragmas, final Expr expr) {
    super(info, expr);
    this.pragmas = pragmas;
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final ArrayList<Object> cache = new ArrayList<>();
    for(final Pragma p : pragmas) cache.add(p.init(cc.qc, info));
    try {
      expr = expr.compile(cc);
    } finally {
      int c = 0;
      for(final Pragma p : pragmas) p.finish(cc.qc, cache.get(c++));
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    seqType = expr.seqType();
    size = expr.size();
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(value(qc));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ArrayList<Object> cache = new ArrayList<>();
    for(final Pragma p : pragmas) cache.add(p.init(qc, info));
    try {
      return qc.value(expr);
    } finally {
      int c = 0;
      for(final Pragma p : pragmas) p.finish(qc, cache.get(c++));
    }
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Pragma[] prag = pragmas.clone();
    final int pl = prag.length;
    for(int p = 0; p < pl; p++) prag[p] = prag[p].copy();
    return copyType(new Extension(info, prag, expr.copy(cc, vm)));
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Pragma p : pragmas) if(p.has(flag)) return true;
    return super.has(flag);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Extension &&
        Array.equals(pragmas, ((Extension) obj).pragmas) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), pragmas, expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Pragma p : pragmas) sb.append(p).append(' ');
    return sb.append(CURLY1 + ' ').append(expr).append(' ').append(CURLY2).toString();
  }
}
