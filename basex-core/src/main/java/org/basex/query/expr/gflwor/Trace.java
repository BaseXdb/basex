package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code trace} clause, emitting diagnostic output per tuple without affecting the stream.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Trace extends Clause {
  /** Traced expression. */
  Expr expr;

  /**
   * Constructor.
   * @param expr traced expression
   * @param info input info (can be {@code null})
   */
  public Trace(final Expr expr, final InputInfo info) {
    super(info, Types.ITEM_ZM);
    this.expr = expr;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(!sub.next(qc)) return false;
        try {
          qc.trace(null, expr.value(qc));
        } catch(final QueryException ex) {
          qc.trace(null, ex::getLocalizedMessage);
        }
        return true;
      }
    };
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Flag flag : flags) {
      if(flag == Flag.NDT) return true;
    }
    return expr.has(flags);
  }

  @Override
  public Trace compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public Trace optimize(final CompileContext cc) {
    return this;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return expr.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public Clause inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined == null) return null;
    expr = inlined;
    return optimize(ic.cc);
  }

  @Override
  public Trace copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Trace(expr.copy(cc, vm), info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof final Trace trc && expr.equals(trc.expr);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(TRACE).token(expr);
  }
}
