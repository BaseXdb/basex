package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.SeqIter;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Or expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Or extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Or(final InputInfo ii, final Expr[] e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    for(int e = 0; e < expr.length; e++) {
      if(!expr[e].value()) continue;

      if(expr[e].ebv(ctx, input).bool(input)) {
        // atomic items can be pre-evaluated
        ctx.compInfo(OPTTRUE, expr[e]);
        return Bln.TRUE;
      }
      ctx.compInfo(OPTFALSE, expr[e]);
      expr = Array.delete(expr, e--);
      if(expr.length == 0) return Bln.FALSE;
    }

    // combines two position expressions
    if(expr.length == 2 && expr[0] instanceof Pos && expr[1] instanceof Pos)
      return ((Pos) expr[0]).union((Pos) expr[1], input);

    // one expression left
    if(expr.length == 1) {
      final SeqType ret = expr[0].returned(ctx);
      if(ret.type == Type.BLN && ret.one()) return expr[0];
    }

    // tries to create a comparison expression
    for(final Expr e : expr) if(!(e instanceof CmpG)) return this;

    final CmpG e1 = (CmpG) expr[0];
    final SeqIter cmp = new SeqIter();

    for(final Expr e : expr) {
      // check comparison operators and left operands for equality
      final CmpG e2 = (CmpG) e;
      if(!e2.exprAndItem(false) || e1.cmp != e2.cmp ||
         !e1.expr[0].sameAs(e2.expr[0])) return this;
      cmp.add((Item) e2.expr[1]);
    }

    ctx.compInfo(OPTWRITE, this);
    return new CmpG(input, e1.expr[0], cmp.finish(), e1.cmp);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    double d = 0;
    boolean f = false;
    for(final Expr e : expr) {
      final Item it = e.ebv(ctx, input);
      if(it.bool(input)) {
        final double s = it.score();
        if(s == 0) return Bln.TRUE;
        d = ctx.score.or(d, s);
        f = true;
      }
    }
    return d == 0 ? Bln.get(f) : Bln.get(d);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    int is = 0;
    for(final Expr e : expr) {
      if(!e.indexAccessible(ic) || ic.seq) return false;
      is += ic.is;
    }
    ic.is = is;
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    super.indexEquivalent(ic);
    return new Union(input, expr);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
  }

  @Override
  public String toString() {
    return toString(" " + OR + " ");
  }
}
