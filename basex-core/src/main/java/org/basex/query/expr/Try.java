package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Try extends Single {
  /** Catch clauses. */
  private Catch[] catches;

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
    final ExprList exprs = new ExprList(catches.length + 1).add(expr);
    for(final Catch ctch : catches) exprs.add(ctch.expr);
    checkAllUp(exprs.finish());
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    try {
      super.compile(cc);
    } catch(final QueryException ex) {
      if(!ex.isCatchable()) throw ex;
      for(final Catch ctch : catches) {
        // found a matching clause: compile and inline error message
        if(ctch.matches(ex)) return cc.replaceWith(this, ctch.compile(cc).inline(ex, cc));
      }
      throw ex;
    }
    for(final Catch ctch : catches) ctch.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    if(expr instanceof Value) return cc.replaceWith(this, expr);

    // remove duplicates and too specific catch clauses
    final ArrayList<Catch> list = new ArrayList<>();
    final ArrayList<NameTest> tests = new ArrayList<>();
    for(final Catch ctch : catches) {
      if(ctch.simplify(tests, cc)) list.add(ctch);
    }
    catches = list.toArray(new Catch[0]);

    // join types of try and catch expressions
    SeqType st = expr.seqType();
    for(final Catch ctch : catches) {
      if(!Function.ERROR.is(ctch.expr)) st = st.union(ctch.seqType());
    }
    exprType.assign(st);
    return this;
  }

  @Override
  public Data data() {
    final ExprList list = new ExprList(catches.length + 1).add(expr);
    for(final Catch ctch : catches) list.add(ctch);
    return data(list.finish());
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
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = false;
    final CompileContext cc = ic.cc;
    try {
      final Expr inlined = expr.inline(ic);
      if(inlined != null) {
        if(inlined instanceof Value) return cc.replaceWith(this, inlined);
        expr = inlined;
        changed = true;
      }
    } catch(final QueryException qe) {
      if(!qe.isCatchable()) throw qe;
      for(final Catch ctch : catches) {
        if(ctch.matches(qe)) {
          // found a matching clause, inline variable and error message
          final Catch ct = ctch.inline(ic);
          return cc.replaceWith(this, (ct == null ? ctch : ct).inline(qe, cc));
        }
      }
      throw qe;
    }

    for(final Catch ctch : catches) {
      changed |= ctch.inline(ic) != null;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
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
    for(final Catch ctch : catches) {
      if(ctch.has(flags)) return true;
    }
    return super.has(flags);
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
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), expr, catches);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(TRY).brace(expr).tokens(catches);
  }
}
