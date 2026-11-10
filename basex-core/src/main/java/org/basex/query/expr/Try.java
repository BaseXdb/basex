package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
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
  /** Finally clause. */
  private Expr fnlly;
  /** Indicates if the try clause is updating. */
  private boolean updating;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr try expression
   * @param fnlly finally block
   * @param catches catch expressions
   */
  public Try(final InputInfo info, final Expr expr, final Expr fnlly, final Catch... catches) {
    super(info, expr, Types.ITEM_ZM);
    this.catches = catches;
    this.fnlly = fnlly;
  }

  @Override
  public boolean navigational() {
    // the 'finally' clause does not contribute to the result
    return expr.navigational() && Checks.all(catches, ctch -> ctch.expr.navigational());
  }

  @Override
  public void checkUp() throws QueryException {
    // check if no or all try/catch expressions are updating
    final ExprList exprs = new ExprList(catches.length + 2).add(expr);
    for(final Catch ctch : catches) exprs.add(ctch.expr);
    checkAllUp(exprs.add(fnlly).finish());
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    for(final Catch ctch : catches) ctch.compile(cc);
    expr = cc.compileOrError(expr, false);
    fnlly = fnlly.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // remove duplicates and too specific catch clauses
    final ArrayList<Catch> newCatches = new ArrayList<>();
    final ArrayList<Test> tests = new ArrayList<>();
    if(!Checks.all(catches, Catch::global)) {
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
        // the error was raised by fn:error, so its stack trace is of no interest
        final Catch ctch = matches(ex);
        if(ctch != null) e = ctch.inline(ex, cc);
        else if(fnlly == Empty.VALUE) throw ex;
      }
    }
    if(e != null) {
      // try { 1 } catch * { 2 } → 1, try { error() } catch * { 2 } → 2
      if(fnlly == Empty.VALUE) return cc.replaceWith(this, e);
      expr = e;
      catches = new Catch[0];
    }

    // without catch clauses, errors (and the pending updates) are passed on to the outer scope
    updating = catches.length > 0 && expr.has(Flag.UPD);

    // join types of try and catch expressions
    SeqType st = expr.seqType();
    for(final Catch ctch : catches) st = st.union(ctch.seqType());
    exprType.assign(st).data(ExprList.concat(catches, expr));

    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // data(try { <a>{ $x }</a> } catch * { }) → data(try { xs:untypedAtomic($x) } catch * { })
    // only atomizations: other modes may discard errors that the catch clauses would handle
    Expr ex = expr;
    boolean changed = false;
    if(mode.atomizing() || mode == Simplify.STRING_VALUE) {
      ex = expr.simplifyFor(mode, cc);
      changed = ex != expr;
      for(final Catch ctch : catches) {
        final Expr cx = ctch.expr.simplifyFor(mode, cc);
        if(cx != ctch.expr) {
          ctch.expr = cx;
          ctch.optimize(cc);
          changed = true;
        }
      }
    }
    return cc.simplify(this, changed ? new Try(info, ex, fnlly, catches).optimize(cc) : this, mode);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // updates of the try clause are only adopted if it succeeds
    final Updates updates = updating ? qc.updates() : null;
    try {
      if(updates != null) updates.nest();
      Value value = null;
      QueryException error = null;
      try {
        final Value val = cache(expr.value(qc));
        // namespace conflicts of the try clause can be caught (upd:compatibilityCheck)
        if(updates != null) updates.checkNamespaces();
        value = val;
      } catch(final QueryException ex) {
        error = ex;
      } catch(final StackOverflowError | ArraySizeException ex) {
        // the stack has been unwound to this expression, so the catch clause has room to run
        error = limitError(ex, info);
      } finally {
        if(updates != null) updates.unnest(value != null);
      }
      return error != null ? caught(error, qc) : value;
    } finally {
      final Value fnl = fnlly.value(qc);
      if(fnl != Empty.VALUE) throw FINALLY_X.get(info, fnl);
    }
  }

  /**
   * Caches lazy items of the result, so that their errors are raised inside the try clause.
   * @param value result of the try clause
   * @return value
   * @throws QueryException query exception
   */
  private Value cache(final Value value) throws QueryException {
    final Type type = expr.seqType().type;
    if(type.intersect(BasicType.STRING) != null ||
        type.intersect(BasicType.BASE64_BINARY) != null) {
      for(final Item item : value) {
        if(item instanceof Lazy) item.cache(false, info);
      }
    }
    return value;
  }

  /**
   * Evaluates the catch clause that matches the specified error, or raises the error.
   * @param ex query exception
   * @param qc query context
   * @return result of the catch clause
   * @throws QueryException query exception
   */
  private Value caught(final QueryException ex, final QueryContext qc) throws QueryException {
    final Catch ctch = matches(ex);
    if(ctch == null) throw ex;
    Util.debug(ex);
    return ctch.value(qc, ex);
  }

  /**
   * Returns a matching catch clause.
   * @param ex query exception
   * @return catch clause or {@code null}
   */
  private Catch matches(final QueryException ex) {
    if(ex.isCatchable()) {
      for(final Catch ctch : catches) {
        if(ctch.matches(ex)) return ctch;
      }
    }
    return null;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.maximum(var, catches).plus(expr.count(var)).plus(fnlly.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr fnlInlined = fnlly.inline(ic);
    if(fnlInlined != null) fnlly = fnlInlined;
    boolean changed = false;
    for(final Catch ctch : catches) {
      changed |= ctch.inline(ic) != null;
    }
    Expr inlined;
    try {
      inlined = expr.inline(ic);
    } catch(final QueryException ex) {
      inlined = FnError.get(ex);
    } catch(final StackOverflowError | ArraySizeException ex) {
      inlined = FnError.get(limitError(ex, info));
    }
    if(inlined != null) expr = inlined;

    return changed || inlined != null || fnlInlined != null ? optimize(ic.cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Try t = new Try(info, expr.copy(cc, vm), fnlly.copy(cc, vm),
        Arr.copyAll(cc, vm, catches));
    t.updating = updating;
    return copyType(t);
  }

  @Override
  public boolean vacuous() {
    return expr.vacuous() && Checks.all(catches, ctch -> ctch.expr.vacuous()) &&
        fnlly.vacuous();
  }

  @Override
  public boolean ddo() {
    return expr.ddo() && Checks.all(catches, ctch -> ctch.expr.ddo());
  }

  @Override
  public boolean has(final Flag... flags) {
    return Checks.any(catches, ctch -> ctch.has(flags)) || super.has(flags) ||
        fnlly.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final Catch ctch : catches) {
      if(!ctch.inlineable(ic)) return false;
    }
    return super.inlineable(ic) && fnlly.inlineable(ic);
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    // the try clause is no tail position: errors of eliminated calls would not be caught
    if(fnlly == Empty.VALUE) {
      for(final Catch ctch : catches) ctch.markTailCalls(cc);
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitAll(visitor, catches) && fnlly.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = 0;
    for(final Catch ctch : catches) size += ctch.exprSize();
    return size + fnlly.exprSize() + super.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Try t && Arrays.equals(catches, t.catches) &&
        fnlly.equals(t.fnlly) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), expr, catches, fnlly);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(TRY).brace(expr).tokens(catches).token(FINALLY).brace(fnlly);
  }
}
