package org.basex.query.func;

import static org.basex.util.Token.*;
import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.AtomType;
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
public final class FNAcc extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNAcc(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Expr e = expr.length != 0 ? expr[0] : checkCtx(ctx);
    switch(def) {
      case POSITION:
        return Itr.get(ctx.pos);
      case LAST:
        return Itr.get(ctx.size);
      case STRING:
        Item it = e.item(ctx, input);
        if(it == null) return Str.ZERO;
        if(it.func()) FNSTR.thrw(ii, this);
        return it.str() && !it.unt() ? it : Str.get(it.atom(ii));
      case NUMBER:
        return number(ctx.iter(e), ctx);
      case STRING_LENGTH:
        return Itr.get(len(checkEStr(e, ctx)));
      case NORMALIZE_SPACE:
        return Str.get(norm(checkEStr(e, ctx)));
      case NAMESPACE_URI_FROM_QNAME:
        it = e.item(ctx, input);
        if(it == null) return null;
        final QNm qn = (QNm) checkType(it, AtomType.QNM);
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

    if(it.func()) FNATM.thrw(input, this);

    try {
      return it.type == AtomType.DBL ? it : AtomType.DBL.e(it, ctx, input);
    } catch(final QueryException ex) {
      return Dbl.NAN;
    }
  }

  @Override
  public boolean uses(final Use u) {
    final boolean pos = def == Function.POSITION || def == Function.LAST;
    return u == Use.CTX && (expr.length == 0 || pos) ||
      u == Use.POS && pos || super.uses(u);
  }
}
