package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.AtomType;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.query.util.format.FormatParser;
import org.basex.query.util.format.Formatter;
import org.basex.query.util.format.DecFormatter;
import org.basex.util.InputInfo;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNFormat extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFormat(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(def) {
      case FORMAT_INTEGER:  return formatInteger(ctx);
      case FORMAT_NUMBER:   return formatNumber(ctx);
      case FORMAT_DATETIME: return formatDate(AtomType.DTM, ctx);
      case FORMAT_DATE:     return formatDate(AtomType.DAT, ctx);
      case FORMAT_TIME:     return formatDate(AtomType.TIM, ctx);
      default:              return super.item(ctx, ii);
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
    final byte[] pic = checkStr(expr[1], ctx);
    final byte[] lng = expr.length == 2 ? EMPTY : checkStr(expr[2], ctx);

    if(expr[0].isEmpty()) return Str.ZERO;
    final long num = checkItr(expr[0], ctx);

    if(pic.length == 0) WRONGINT.thrw(input, pic);
    final FormatParser fp = new FormatParser(input, pic, null);
    return Str.get(Formatter.get(string(lng)).formatInt(num, fp));
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
    else if(!it.type.isUntyped() && !it.type.isNumber()) Err.number(this, it);

    final String pic = string(checkStr(expr[1], ctx));
    final QNm frm = new QNm(expr.length == 3 ?
        checkStr(expr[2], ctx) : EMPTY, ctx);

    final DecFormatter df = ctx.decFormats.get(frm.eqname());
    if(df == null) throw FORMNUM.thrw(input, frm);
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
    final byte[] pic = checkEStr(expr[1], ctx);
    final byte[] lng = expr.length == 5 ? checkEStr(expr[2], ctx) : EMPTY;
    final byte[] cal = expr.length == 5 ? checkEStr(expr[3], ctx) : EMPTY;
    final byte[] plc = expr.length == 5 ? checkEStr(expr[4], ctx) : EMPTY;
    if(it == null) return null;
    final Date date = (Date) checkType(it, tp);

    final Formatter form = Formatter.get(string(lng));
    return Str.get(form.formatDate(date, pic, cal, plc, input));
  }
}
