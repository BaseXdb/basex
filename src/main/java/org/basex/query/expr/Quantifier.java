package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.flwor.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Some/Every satisfier clause.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Quantifier extends ParseExpr {
  /** Every flag. */
  private final boolean every;
  /** For/Let expressions. */
  private final For[] fl;
  /** Satisfier. */
  private Expr sat;

  /**
   * Constructor.
   * @param ii input info
   * @param f variable inputs
   * @param s satisfier
   * @param e every flag
   */
  public Quantifier(final InputInfo ii, final For[] f, final Expr s, final boolean e) {
    super(ii);
    sat = s;
    fl = f;
    every = e;
    type = SeqType.BLN;
  }

  @Override
  public void checkUp() throws QueryException {
    for(final For f : fl) f.checkUp();
    checkNoUp(sat);
  }

  @Override
  public Expr analyze(final AnalyzeContext ctx) throws QueryException {
    for(final For f : fl) f.analyze(ctx);
    sat = sat.analyze(ctx);
    return this;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    // compile for clauses
    final int vs = ctx.vars.size();
    for(final For f : fl) f.compile(ctx);
    sat = sat.compile(ctx).compEbv(ctx);
    ctx.vars.size(vs);

    // find empty sequences
    boolean empty = false;
    for(final For f : fl) empty |= f.isEmpty();

    // return pre-evaluated result
    return empty ? optPre(Bln.get(every), ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter[] iter = new Iter[fl.length];
    for(int f = 0; f < fl.length; ++f) iter[f] = ctx.iter(fl[f]);
    return Bln.get(iter(ctx, iter, 0));
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx query context
   * @param it iterator
   * @param p variable position
   * @return satisfied flag
   * @throws QueryException query exception
   */
  private boolean iter(final QueryContext ctx, final Iter[] it, final int p)
      throws QueryException {

    final boolean last = p + 1 == fl.length;
    while(it[p].next() != null) {
      if(every ^ (last ? sat.ebv(ctx, info).bool(info) :
        iter(ctx, it, p + 1))) {
        for(final Iter ri : it) ri.reset();
        return !every;
      }
    }
    return every;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || sat.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final ForLet f : fl) c += f.count(v);
    return c + sat.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final ForLet f : fl) if(!f.removable(v)) return false;
    return sat.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final ForLet f : fl) f.remove(v);
    sat = sat.remove(v);
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, every ? EVERY : SOME), fl, sat);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(every ? EVERY : SOME);
    for(final For f : fl) sb.append(' ').append(f);
    return sb.append(' ' + SATISFIES + ' ' + sat).toString();
  }
}
