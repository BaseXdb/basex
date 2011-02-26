package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.query.util.format.DateFormatter;
import org.basex.query.util.format.IntFormatter;
import org.basex.query.util.format.DecimalFormat;
import org.basex.util.InputInfo;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(def) {
      case FORMINT: return formatInteger(ctx);
      case FORMNUM: return formatNumber(ctx);
      case FORMDTM: return formatDate(Type.DTM, ctx);
      case FORMDAT: return formatDate(Type.DAT, ctx);
      case FORMTIM: return formatDate(Type.TIM, ctx);
      default:      return super.item(ctx, ii);
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }

  /**
   * Returns a formatted integer.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatInteger(final QueryContext ctx) throws QueryException {
    final String pic = string(checkStr(expr[1], ctx));
    if(pic.isEmpty()) WRONGINT.thrw(input, pic);
    if(expr[0].empty()) return Str.ZERO;

    final byte[] lang = expr.length == 2 ? EMPTY : checkStr(expr[2], ctx);
    final long num = checkItr(expr[0], ctx);
    final byte[] str = IntFormatter.format(num, pic, string(lang));
    if(str == null) PICDATE.thrw(input, pic);
    return Str.get(str);
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatNumber(final QueryContext ctx) throws QueryException {
    // evaluate arguments
    Item it = expr[0].item(ctx, input);
    if(it == null) it = Dbl.NAN;
    else if(!it.unt() && !it.num()) Err.number(this, it);

    final String pic = string(checkStr(expr[1], ctx));
    final QNm frm = new QNm(expr.length == 3 ? checkStr(expr[2], ctx) : EMPTY);
    final DecimalFormat df = ctx.decFormats.get(frm);
    if(df == null) FORMNUM.thrw(input, frm);
    return Str.get(df.format(input, it, pic));
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

    final Item it = expr[0].item(ctx, input);
    if(it == null) return null;
    final Date date = (Date) checkType(it, tp);
    final String pic = string(checkEStr(expr[1], ctx));
    final byte[] lng = expr.length == 5 ? checkEStr(expr[2], ctx) : EMPTY;
    final byte[] cal = expr.length == 5 ? checkEStr(expr[3], ctx) : EMPTY;
    final byte[] plc = expr.length == 5 ? checkEStr(expr[4], ctx) : EMPTY;
    return Str.get(DateFormatter.format(date, pic, lng, cal, plc, input));
  }
}
