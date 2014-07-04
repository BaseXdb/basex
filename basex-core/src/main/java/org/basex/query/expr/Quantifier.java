package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Some/Every satisfier clause.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Quantifier extends Single {
  /** Every flag. */
  private final boolean every;

  /**
   * Constructor.
   * @param info input info
   * @param inputs variable inputs
   * @param expr satisfier
   * @param every every flag
   */
  public Quantifier(final InputInfo info, final For[] inputs, final Expr expr,
      final boolean every) {
    this(info, new GFLWOR(info, new LinkedList<GFLWOR.Clause>(Arrays.asList(inputs)),
        compBln(expr, info)), every);
  }

  /**
   * Copy constructor.
   * @param info input info
   * @param tests expression
   * @param every every flag
   */
  private Quantifier(final InputInfo info, final Expr tests, final boolean every) {
    super(info, tests);
    this.every = every;
    type = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // return pre-evaluated result
    if(expr.isValue()) return optPre(item(qc, info), qc);

    // pre-evaluate satisfy clause if it is a value
    if(expr instanceof GFLWOR && !expr.has(Flag.NDT) && !expr.has(Flag.UPD)) {
      final GFLWOR gflwor = (GFLWOR) expr;
      if(gflwor.size() > 0 && gflwor.ret.isValue()) {
        final Value value = (Value) gflwor.ret;
        qc.compInfo(OPTPRE, value);
        return Bln.get(value.ebv(qc, info).bool(info));
      }
    }
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = expr.iter(qc);
    for(Item it; (it = iter.next()) != null;)
      if(every ^ it.ebv(qc, ii).bool(ii)) return Bln.get(!every);
    return Bln.get(every);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Quantifier(info, expr.copy(qc, scp, vs), every);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, every ? EVERY : SOME), expr);
  }

  @Override
  public String toString() {
    return every ? EVERY : SOME + '(' + expr + ')';
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }
}
