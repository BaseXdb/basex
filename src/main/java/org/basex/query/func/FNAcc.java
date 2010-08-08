package org.basex.query.func;

import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Accessor functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNAcc extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNAcc(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Expr e = expr.length != 0 ? expr[0] : checkCtx(ctx);
    switch(func) {
      case POS:
        return Itr.get(ctx.pos);
      case LAST:
        return Itr.get(ctx.size);
      case STRING:
        Item it = e.atomic(ctx, input);
        return it == null ? Str.ZERO : it.str() && !it.unt() ? it :
          Str.get(it.atom());
      case NUMBER:
        final Iter ir = ctx.iter(e);
        it = ir.next();
        return it == null || ir.next() != null ? Dbl.NAN : number(it, ctx);
      case STRLEN:
        return Itr.get(len(checkEStr(e, ctx)));
      case NORM:
        return Str.get(norm(checkEStr(e, ctx)));
      case URIQNAME:
        it = e.atomic(ctx, input);
        if(it == null) return null;
        return ((QNm) checkType(it, Type.QNM)).uri;
      default:
        return super.atomic(ctx, ii);
    }
  }

  /**
   * Converts the specified item to a double.
   * @param it input item
   * @param ctx query context
   * @return double iterator
   */
  private Item number(final Item it, final QueryContext ctx) {
    final double d = Double.NaN;
    if(it != null) {
      try {
        return it.type == Type.DBL ? it : Type.DBL.e(it, ctx, input);
      } catch(final QueryException ex) {
      }
    }
    return Dbl.get(d);
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    final boolean pos = func == FunDef.POS || func == FunDef.LAST;
    return u == Use.CTX && (pos || expr.length == 0) ||
      u == Use.POS && pos || super.uses(u, ctx);
  }
}
