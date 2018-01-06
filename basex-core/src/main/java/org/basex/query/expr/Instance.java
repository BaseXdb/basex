package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Instance test.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Instance extends Single {
  /** Sequence type to check for. */
  private final SeqType instType;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param instType sequence type to check for
   */
  public Instance(final InputInfo info, final Expr expr, final SeqType instType) {
    super(info, expr, SeqType.BLN_O);
    this.instType = instType;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return expr.seqType().instanceOf(instType) ? cc.replaceWith(this, Bln.TRUE) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(instType.instance(expr.value(qc)));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Instance(info, expr.copy(cc, vm), instType);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Instance && instType.eq(((Instance) obj).instType) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OF, instType), expr);
  }

  @Override
  public String toString() {
    return Util.info("% instance of %", expr, instType);
  }
}
