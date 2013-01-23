package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Some/Every satisfier clause.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Quantifier extends Single {
  /** Every flag. */
  private final boolean every;

  /**
   * Constructor.
   * @param ii input info
   * @param f variable inputs
   * @param s satisfier
   * @param e every flag
   */
  public Quantifier(final InputInfo ii, final For[] f, final Expr s, final boolean e) {
    super(ii, new GFLWOR(ii, new LinkedList<GFLWOR.Clause>(Arrays.asList(f)),
        compBln(s, ii)));
    every = e;
    type = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    // compile for clauses
    super.compile(ctx, scp);

    // return pre-evaluated result
    return expr.size() == 0 ? optPre(Bln.get(every), ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter iter = expr.iter(ctx);
    for(Item it; (it = iter.next()) != null;)
      if(every ^ it.ebv(ctx, ii).bool(ii)) return Bln.get(!every);
    return Bln.get(every);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, every ? EVERY : SOME), expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(every ? EVERY : SOME);
    return sb.append('(').append(expr).append(')').toString();
  }
}
