package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.util.IndexContext;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.ft.Scoring;

/**
 * Or expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Or extends Logical {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Or(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // remove atomic values
    final Expr c = super.comp(ctx);
    if(c != this) return c;

    // merge predicates if possible
    CmpG cmpg = null;
    Expr[] ex = {};
    for(final Expr e : expr) {
      Expr tmp = null;
      if(e instanceof CmpG) {
        // merge general comparisons
        final CmpG g = (CmpG) e;
        if(cmpg == null) cmpg = g;
        else if(cmpg.union(g, ctx)) tmp = g;
      }
      // no optimization found; add original expression
      if(tmp == null) ex = Array.add(ex, e);
    }
    if(ex.length != expr.length) ctx.compInfo(OPTWRITE, this);
    expr = ex;
    compFlatten(ctx);

    // return single expression if it yields a boolean
    return expr.length == 1 ? compBln(expr[0]) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    double d = 0;
    boolean f = false;
    for(final Expr e : expr) {
      final Item it = e.ebv(ctx, input);
      if(it.bool(input)) {
        final double s = it.score();
        if(s == 0) return Bln.TRUE;
        d = Scoring.or(d, s);
        f = true;
      }
    }
    return d == 0 ? Bln.get(f) : Bln.get(d);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    int is = 0;
    Expr[] exprs = {};
    boolean ia = true;
    for(final Expr e : expr) {
      if(e.indexAccessible(ic) && !ic.seq) {
        // skip expressions without results
        if(ic.costs() == 0) continue;
        is += ic.costs();
      } else {
        ia = false;
      }
      exprs = Array.add(exprs, e);
    }
    ic.costs(is);
    expr = exprs;
    return ia;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    super.indexEquivalent(ic);
    return new Union(input, expr);
  }

  @Override
  public String toString() {
    return toString(" " + OR + " ");
  }
}
