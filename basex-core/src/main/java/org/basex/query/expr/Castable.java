package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Castable expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Castable extends Single {
  /** Static context. */
  private final StaticContext sc;
  /** Instance. */
  private final SeqType seq;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param e expression
   * @param seq sequence type
   */
  public Castable(final StaticContext sc, final InputInfo info, final Expr e, final SeqType seq) {
    super(info, e);
    this.sc = sc;
    this.seq = seq;
    type = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    return expr.isValue() ? preEval(qc) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value v = expr.value(qc);
    return Bln.get(seq.occ.check(v.size()) &&
        (v.isEmpty() || seq.cast((Item) v, qc, sc, info, false) != null));
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Castable(sc, info, expr.copy(qc, scp, vs), seq);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, seq), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CASTABLE + ' ' + AS + ' ' + seq;
  }
}
