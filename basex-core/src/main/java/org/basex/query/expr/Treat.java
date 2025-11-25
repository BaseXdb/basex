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
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param seqType sequence type
   */
  public Treat(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, seqType);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType st = seqType(), et = expr.seqType();

    // skip check if return type is already correct
    if(et.instanceOf(st)) {
      cc.info(OPTTYPE_X_X, st, expr);
      return expr;
    }
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    final SeqType st = seqType();
    if(st.instance(value)) return value;
    throw NOTREAT_X_X_X.get(info, expr.seqType(), st, expr.toErrorString());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Treat(info, expr.copy(cc, vm), seqType()));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Treat trt && seqType().eq(trt.seqType()) &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, AS, seqType()), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("(").token(expr).token(TREAT).token(AS).token(seqType()).token(')');
  }
}
