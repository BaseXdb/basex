package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Treat as expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Treat extends Single {
  /** Sequence type to check. */
  private final SeqType check;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param seqType sequence type
   */
  public Treat(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, seqType.matched());
    check = seqType;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // skip check if return type is already correct
    if(expr.seqType().instanceOf(check)) return cc.replaceWith(this, expr);
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    if(check.instance(value)) return value;
    throw NOTREAT_X_X_X.get(info, expr.seqType(), check, expr);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Treat(info, expr.copy(cc, vm), check));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Treat trt && check.eq(trt.check) &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, AS, check), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("(").token(expr).token(TREAT).token(AS).token(check).token(')');
  }
}
