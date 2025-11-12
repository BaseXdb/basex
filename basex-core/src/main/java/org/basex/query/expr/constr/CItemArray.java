package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Item array constructor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CItemArray extends Single {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr array members
   */
  public CItemArray(final InputInfo info, final Expr expr) {
    super(info, expr, Types.ARRAY_O);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // [ $value ], array { $item } → util:array-member(...)
    final long size = expr.size();
    if(size == 0) return XQArray.empty();
    if(size == 1) return cc.replaceWith(this, cc.function(_UTIL_ARRAY_MEMBER, info, expr));

    exprType.assign(ArrayType.get(expr.seqType().with(Occ.EXACTLY_ONE)));
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return XQArray.items(expr.value(qc));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr ex = this;
    // number(array { 1 }) → number(1)
    if(mode.oneOf(Simplify.NUMBER, Simplify.DATA)) ex = expr.simplifyFor(mode, cc);
    return cc.simplify(this, ex, mode);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CItemArray(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CItemArray && super.equals(obj);
  }

  @Override
  public String description() {
    return "curly " + ARRAY + " constructor";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(ARRAY).brace(expr);
  }
}
