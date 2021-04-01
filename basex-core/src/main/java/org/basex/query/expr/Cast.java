package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cast expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /** Static context. */
  private final StaticContext sc;
  /** Sequence type to cast to (zero or one items). */
  final SeqType seqType;

  /**
   * Function constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   * @param seqType sequence type to cast to (zero or one items)
   */
  public Cast(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType) {
    super(info, expr, SeqType.ITEM_ZM);
    this.sc = sc;
    this.seqType = seqType;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.STRING, cc);

    if((ZERO_OR_ONE.is(expr) || EXACTLY_ONE.is(expr) || ONE_OR_MORE.is(expr)) &&
        seqType.occ.instanceOf(expr.seqType().occ)) expr = expr.arg(0);

    // target type
    final SeqType est = expr.seqType();
    Type dt = seqType.type;
    Occ o = seqType.occ;
    if(dt instanceof ListType) {
      dt = dt.atomic();
      o = Occ.ZERO_OR_MORE;
    } else if(o == Occ.ZERO_OR_ONE && est.oneOrMore() && !est.mayBeArray()) {
      o = Occ.EXACTLY_ONE;
    }
    exprType.assign(dt, o);

    if(!est.mayBeArray()) {
      final long es = expr.size();
      if(es != -1 && (es < o.min || es > o.max)) throw error(expr);

      final Type et = est.type;
      if(et.instanceOf(dt)) {
        if(est.occ.instanceOf(o) && (et.eq(dt) || dt == AtomType.NUMERIC))
          return cc.replaceWith(this, expr);
      }
    }
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return seqType.cast(expr.atomValue(qc, info), true, qc, sc, info);
  }

  /**
   * Throws a type error.
   * @param ex expression that triggers the error
   * @return query exception
   */
  private QueryException error(final Expr ex) {
    return INVTYPE_X_X_X.get(info, ex.seqType(), seqType, ex);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return simplifyForCast(mode, cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Cast(sc, info, expr.copy(cc, vm), seqType));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Cast && seqType.eq(((Cast) obj).seqType) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, AS, seqType), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    if(seqType.one()) {
      qs.token("(").token(expr).token(CAST).token(AS).token(seqType).token(')');
    } else {
      qs.token(seqType.type).paren(expr);
    }
  }
}
