package org.basex.query.func;

import static org.basex.util.Token.*;
import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.QNm;
import org.basex.query.item.AtomType;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
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
        return Int.get(ctx.pos);
      case LAST:
        return Int.get(ctx.size);
      case STRING:
        return string(e, ii, ctx);
      case NUMBER:
        return number(ctx.iter(e), ctx);
      case STRING_LENGTH:
        return Int.get(len(checkEStr(expr.length == 0 ?
            string(e, ii, ctx) : e, ctx)));
      case NORMALIZE_SPACE:
        return Str.get(norm(checkEStr(e, ctx)));
      case NAMESPACE_URI_FROM_QNAME:
        final Item it = e.item(ctx, input);
        return it == null ? null :
          Uri.uri(((QNm) checkType(it, AtomType.QNM)).uri());
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Converts the specified item to a string.
   * @param e expression
   * @param ii input info
   * @param ctx query context
   * @return double iterator
   * @throws QueryException query exception
   */
  private Item string(final Expr e, final InputInfo ii,
      final QueryContext ctx) throws QueryException {

    final Item it = e.item(ctx, input);
    if(it == null) return Str.ZERO;
    final Type t = it.type;
    if(t.isFunction()) FNSTR.thrw(ii, this);
    return t == AtomType.STR ? it : Str.get(it.string(ii));
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
    final Type t = it.type;
    if(t.isFunction()) FNATM.thrw(input, this);
    try {
      return t == AtomType.DBL ? it : AtomType.DBL.e(it, ctx, input);
    } catch(final QueryException ex) {
      return Dbl.NAN;
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && expr.length == 0 ||
        u == Use.POS && (def == Function.POSITION || def == Function.LAST) ||
        super.uses(u);
  }
}
