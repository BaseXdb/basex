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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Cast extends Convert {
  /**
   * Function constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   * @param seqType sequence type to cast to (zero or one item)
   */
  public Cast(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType) {
    super(sc, info, expr, seqType, SeqType.ITEM_ZM);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);
    if((ZERO_OR_ONE.is(expr) || EXACTLY_ONE.is(expr) || ONE_OR_MORE.is(expr)) &&
        seqType.occ.instanceOf(expr.seqType().occ)) expr = expr.arg(0);

    final SeqType castType = castType();
    exprType.assign(castType);

    final Boolean castable = cast(castType);
    if(castable == Boolean.FALSE) throw error(expr);
    if(castable == Boolean.TRUE) return cc.replaceWith(this, expr);

    final Expr arg = simplify(castType, cc);
    if(arg != null) return new Cast(sc, info, arg, seqType).optimize(cc);

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
    return INVCONVERT_X_X_X.get(info, ex.seqType(), seqType, ex);
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
  public void toString(final QueryString qs) {
    if(seqType.one()) {
      qs.token("(").token(expr).token(CAST).token(AS).token(seqType).token(')');
    } else {
      qs.token(seqType.type).paren(expr);
    }
  }
}
