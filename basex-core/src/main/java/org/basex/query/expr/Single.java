package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract single expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Single extends ParseExpr {
  /** Expression. */
  public Expr expr;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param seqType sequence type
   */
  protected Single(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, seqType);
    this.expr = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return this;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
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
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined == null) return null;
    expr = inlined;
    return optimize(ic.cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + 1;
  }

  /**
   * Simplify casts.
   * @param mode mode of simplification
   * @param cc compilation context
   * @return simplified expression
   * @throws QueryException query exception
   */
  final Expr simplifyForCast(final Simplify mode, final CompileContext cc) throws QueryException {
    final SeqType est = expr.seqType(), dst = seqType();
    if(est.occ.instanceOf(dst.occ)) {
      final Type et = est.type, dt = dst.type;
      if(mode == Simplify.STRING && et.isStringOrUntyped() &&
           dt.oneOf(AtomType.STRING, AtomType.UNTYPED_ATOMIC) ||
         mode == Simplify.NUMBER && (et.isUntyped() && dt == AtomType.DOUBLE ||
           et.instanceOf(AtomType.INT) && et.instanceOf(dt)) ||
         mode == Simplify.DATA && et instanceof NodeType && dt == AtomType.UNTYPED_ATOMIC) {
        return cc.simplify(this, expr);
      }
    }
    return super.simplifyFor(mode, cc);
  }

  /**
   * {@inheritDoc}
   * Must be overwritten by implementing class.
   */
  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Single && expr.equals(((Single) obj).expr);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), expr);
  }
}
