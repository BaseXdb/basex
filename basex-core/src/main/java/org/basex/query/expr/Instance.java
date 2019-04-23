package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Instance test.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Sequence type to check for. */
  private final SeqType seqType;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param seqType sequence type to check for
   */
  public Instance(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, SeqType.BLN_O);
    this.seqType = seqType;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    final SeqType st = expr.seqType();
    Expr ex = this;
    if(st.instanceOf(seqType)) {
      ex = Bln.TRUE;
    } else if(!st.couldBe(seqType)) {
      // if no intersection is possible at compile time, final type cannot be an instance either
      ex = Bln.FALSE;
    }
    return cc.replaceWith(this, ex);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(seqType.instance(expr.value(qc)));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Instance(info, expr.copy(cc, vm), seqType);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Instance && seqType.eq(((Instance) obj).seqType) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, OF, seqType), expr);
  }

  @Override
  public String toString() {
    return Util.info("% instance of %", expr, seqType);
  }
}
