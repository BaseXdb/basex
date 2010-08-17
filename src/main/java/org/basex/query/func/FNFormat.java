package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.query.util.format.DateFormatter;
import org.basex.query.util.format.IntFormatter;
import org.basex.query.util.format.NumFormatter;
import org.basex.util.InputInfo;

/**
 * Formatting functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNFormat extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNFormat(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case FORMINT: return formatInteger(ctx);
      case FORMNUM: return formatNumber(ctx);
      case FORMDTM: return formatDate(Type.DTM, ctx);
      case FORMDAT: return formatDate(Type.DAT, ctx);
      case FORMTIM: return formatDate(Type.TIM, ctx);
      default:      return super.atomic(ctx, ii);
    }
  }

  /**
   * Returns a formatted integer.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatInteger(final QueryContext ctx) throws QueryException {
    final String pic = string(checkEStr(expr[1], ctx));
    if(expr[0].empty()) return Str.ZERO;

    final byte[] lang = expr.length == 2 ? EMPTY : checkEStr(expr[2], ctx);
    final long num = checkItr(expr[0], ctx);

    return Str.get(IntFormatter.format(num, pic, string(lang)));
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatNumber(final QueryContext ctx) throws QueryException {
    // evaluate arguments
    Item it = expr[0].atomic(ctx, input);
    if(it == null) it = Dbl.NAN;
    else if(!it.unt() && !it.num()) Err.number(this, it);

    final String pic = string(checkEStr(expr[1], ctx));
    if(expr.length == 3) Err.or(input, FORMNUM, expr[2]);

    return Str.get(NumFormatter.format(input, it, pic));
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @param tp input type
   * @return string
   * @throws QueryException query exception
   */
  private Item formatDate(final Type tp, final QueryContext ctx)
      throws QueryException {

    final Item it = expr[0].atomic(ctx, input);
    if(it == null) return null;
    final Date date = (Date) checkType(it, tp);
    final String pic = string(checkEStr(expr[1], ctx));
    final byte[] lng = expr.length == 5 ? checkEStr(expr[2], ctx) : EMPTY;
    final byte[] cal = expr.length == 5 ? checkEStr(expr[3], ctx) : EMPTY;
    final byte[] plc = expr.length == 5 ? checkEStr(expr[4], ctx) : EMPTY;
    return Str.get(DateFormatter.format(date, pic, lng, cal, plc, input));
  }
}
