package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Project specific try/catch expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Catch clauses. */
  private Catch[] catches;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr try expression
   * @param catches catch expressions
   */
  public Try(final InputInfo info, final Expr expr, final Catch... catches) {
    super(info, expr, SeqType.ITEM_ZM);
    this.catches = catches;
  }

  @Override
  public void checkUp() throws QueryException {
    // check if no or all try/catch expressions are updating
    final ExprList exprs = new ExprList(catches.length + 1).add(expr);
    for(final Catch ctch : catches) exprs.add(ctch.expr);
    checkAllUp(exprs.finish());
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    for(final Catch ctch : catches) ctch.compile(cc);
    try {
      super.compile(cc);
    } catch(final QueryException ex) {
      expr = cc.error(ex, expr);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // remove duplicates and too specific catch clauses
    final ArrayList<Catch> newCatches = new ArrayList<>();
    final ArrayList<Test> tests = new ArrayList<>();
    if(!((Checks<Catch>) Catch::global).all(catches)) {
      for(final Catch ctch : catches) {
        if(ctch.simplify(tests, cc)) newCatches.add(ctch);
      }
      catches = newCatches.toArray(Catch[]::new);
    }

    Expr e = null;
    if(expr instanceof Value) {
      e = expr;
    } else if(Function.ERROR.is(expr) && ((FnError) expr).values(true, cc)) {
      try {
        expr.value(cc.qc);
      } catch(final QueryException ex) {
        Util.debug(ex);
        if(!ex.isCatchable()) throw ex;
        final Catch ctch = matches(ex);
        if(ctch != null) e = ctch.inline(ex, cc);
        else throw ex;
      }
    }
    if(e != null) {
      expr = e;
      catches = new Catch[0];
    }

    // join types of try and catch expressions
    SeqType st = expr.seqType();
    for(final Catch ctch : catches) st = st.union(ctch.seqType());
    exprType.assign(st).data(ExprList.concat(catches, expr));

    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return expr.value(qc);
    } catch(final QueryException ex) {
      Util.debug(ex);
      final Catch ctch = matches(ex);
      if(ctch != null) return ctch.value(qc, ex);
      throw ex;
    }
  }

  /**
   * Returns a matching catch clause.
   * @param ex query exception
   * @return catch clause or {@code null}
   */
  private Catch matches(final QueryException ex) {
    for(final Catch ctch : catches) {
      if(ctch.matches(ex)) return ctch;
    }
    return null;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.maximum(var, catches).plus(expr.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = false;
    for(final Catch ctch : catches) {
      changed |= ctch.inline(ic) != null;
    }
    Expr inlined = null;
    try {
      inlined = expr.inline(ic);
    } catch(final QueryException ex) {
      inlined = ic.cc.error(ex, expr);
    }
    if(inlined != null) expr = inlined;

    return changed || inlined != null ? optimize(ic.cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Try(info, expr.copy(cc, vm), Arr.copyAll(cc, vm, catches)));
  }

  @Override
  public boolean vacuous() {
    return expr.vacuous() && ((Checks<Catch>) ctch -> ctch.expr.vacuous()).all(catches);
  }

  @Override
  public boolean ddo() {
    return expr.ddo() && ((Checks<Catch>) ctch -> ctch.expr.ddo()).all(catches);
  }

  @Override
  public boolean has(final Flag... flags) {
    return ((Checks<Catch>) ctch -> ctch.has(flags)).any(catches) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final Catch ctch : catches) {
      if(!ctch.inlineable(ic)) return false;
    }
    return super.inlineable(ic);
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    expr.markTailCalls(cc);
    for(final Catch ctch : catches) ctch.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, catches);
  }

  @Override
  public int exprSize() {
    int size = 0;
    for(final Catch ctch : catches) size += ctch.exprSize();
    return size + super.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Try)) return false;
    final Try t = (Try) obj;
    return Array.equals(catches, t.catches) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), expr, catches);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(TRY).brace(expr).tokens(catches);
  }
}
