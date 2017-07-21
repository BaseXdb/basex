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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Castable extends Single {
  /** Static context. */
  private final StaticContext sc;
  /** Sequence type to check for. */
  private final SeqType type;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   * @param type sequence type to check for
   */
  public Castable(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType type) {
    super(info, expr);
    this.sc = sc;
    this.type = type;
    seqType = SeqType.BLN;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return expr.isValue() ? cc.preEval(this) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value v = qc.value(expr);
    return Bln.get(type.occ.check(v.size()) &&
        (v.isEmpty() || type.cast((Item) v, qc, sc, info, false) != null));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Castable(sc, info, expr.copy(cc, vm), type);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, type), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CASTABLE + ' ' + AS + ' ' + type;
  }
}
