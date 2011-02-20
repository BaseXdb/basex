package org.basex.query.func;

import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.SimpleType;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Accessor functions.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Expr e = expr.length != 0 ? expr[0] : checkCtx(ctx);
    switch(def) {
      case POS:
        return Itr.get(ctx.pos);
      case LAST:
        return Itr.get(ctx.size);
      case STRING:
        Item it = e.item(ctx, input);
        return it == null ? Str.ZERO : it.str() && !it.unt() ? it :
          Str.get(it.atom());
      case NUMBER:
        return number(ctx.iter(e), ctx);
      case STRLEN:
        return Itr.get(len(checkEStr(e, ctx)));
      case NORM:
        return Str.get(norm(checkEStr(e, ctx)));
      case URIQNAME:
        it = e.item(ctx, input);
        if(it == null) return null;
        final QNm qn = (QNm) checkType(it, SimpleType.QNM);
        return qn.hasUri() ? qn.uri() :
          Uri.uri(ctx.ns.uri(qn.pref(), true, ii));
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Converts the specified item to a double.
   * @param ir iterator
   * @param ctx query context
   * @return double iterator
   * @throws QueryException query exception
   */
  private Item number(final Iter ir, final QueryContext ctx)
      throws QueryException {

    final Item it = ir.next();
    if(it == null || ir.next() != null) return Dbl.NAN;

    try {
      return it.type == SimpleType.DBL ? it : SimpleType.DBL.e(it, ctx, input);
    } catch(final QueryException ex) {
      return Dbl.NAN;
    }
  }

  @Override
  public boolean uses(final Use u) {
    final boolean pos = def == FunDef.POS || def == FunDef.LAST;
    return u == Use.CTX && (pos || expr.length == 0) ||
      u == Use.POS && pos || super.uses(u);
  }
}
