package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Project specific try/catch expression.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Catch clauses. */
  private final Catch[] catches;

  /**
   * Constructor.
   * @param info input info
   * @param expr try expression
   * @param catches catch expressions
   */
  public Try(final InputInfo info, final Expr expr, final Catch[] catches) {
    super(info, expr, SeqType.ITEM_ZM);
    this.catches = catches;
  }

  @Override
  public void checkUp() throws QueryException {
    // check if no or all try/catch expressions are updating
    final int cl = catches.length;
    final Expr[] tmp = new Expr[cl + 1];
    tmp[0] = expr;
    for(int c = 0; c < cl; ++c) tmp[c + 1] = catches[c].expr;
    checkAllUp(tmp);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    try {
      super.compile(cc);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch ctch : catches) {
        // found a matching clause: compile and inline error message
        if(ctch.matches(ex)) return cc.replaceWith(this, ctch.compile(cc).asExpr(ex, cc));
      }
      throw ex;
    }
    for(final Catch ctch : catches) ctch.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    if(expr instanceof Value) return cc.replaceWith(this, expr);

    SeqType st = expr.seqType();
    for(final Catch ctch : catches) {
      if(!ctch.expr.isFunction(Function.ERROR)) st = st.union(ctch.seqType());
    }
    exprType.assign(st);
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // don't catch errors from error handlers
    try {
      return expr.value(qc);
    } catch(final QueryException ex) {
      if(ex.isCatchable()) {
        for(final Catch ctch : catches) {
          if(ctch.matches(ex)) return ctch.value(qc, ex);
        }
      }
      throw ex;
    }
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.maximum(var, catches).plus(expr.count(var));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean changed = false;
    try {
      final Expr sub = expr.inline(var, ex, cc);
      if(sub != null) {
        if(sub instanceof Value) return cc.replaceWith(this, sub);
        expr = sub;
        changed = true;
      }
    } catch(final QueryException qe) {
      if(!qe.isCatchable()) throw qe;
      for(final Catch ctch : catches) {
        if(ctch.matches(qe)) {
          // found a matching clause, inline variable and error message
          final Catch nw = ctch.inline(var, ex, cc);
          return cc.replaceWith(this, (nw == null ? ctch : nw).asExpr(qe, cc));
        }
      }
      throw qe;
    }

    for(final Catch ctch : catches) changed |= ctch.inline(var, ex, cc) != null;
    return changed ? this : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Try(info, expr.copy(cc, vm), Arr.copyAll(cc, vm, catches)));
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Catch ctch : catches) {
      if(ctch.has(flags)) return true;
    }
    return super.has(flags);
  }

  @Override
  public boolean removable(final Var var) {
    for(final Catch ctch : catches) {
      if(!ctch.removable(var)) return false;
    }
    return super.removable(var);
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final Catch ctch : catches) ctch.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, catches);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Catch ctch : catches) size += ctch.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Try && Array.equals(catches, ((Try) obj).catches) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, catches);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("try { " + expr + " }");
    for(final Catch ctch : catches) sb.append(' ').append(ctch);
    return sb.toString();
  }
}
