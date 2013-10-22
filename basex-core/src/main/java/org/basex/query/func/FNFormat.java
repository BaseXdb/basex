package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNFormat extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<FormatParser> formats = new TokenObjMap<FormatParser>();

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFormat(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case FORMAT_INTEGER:  return formatInteger(ctx);
      case FORMAT_NUMBER:   return formatNumber(ctx);
      case FORMAT_DATETIME: return formatDate(AtomType.DTM, ctx);
      case FORMAT_DATE:     return formatDate(AtomType.DAT, ctx);
      case FORMAT_TIME:     return formatDate(AtomType.TIM, ctx);
      default:              return super.item(ctx, ii);
    }
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

    FormatParser fp = formats.get(pic);
    if(fp == null) {
      fp = new IntFormat(pic, info);
      formats.put(pic, fp);
    }
    return Str.get(Formatter.get(lng).formatInt(num, fp));
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatNumber(final QueryContext ctx) throws QueryException {
    // evaluate arguments
    Item it = expr[0].item(ctx, info);
    if(it == null) it = Dbl.NAN;
    else if(!it.type.isNumberOrUntyped()) number(this, it);
    // retrieve picture
    final byte[] pic = checkStr(expr[1], ctx);
    // retrieve format declaration
    final QNm frm = expr.length == 3 ? new QNm(trim(checkEStr(expr[2], ctx)), sc) :
      new QNm(EMPTY);
    final DecFormatter df = sc.decFormats.get(frm.id());
    if(df == null) throw FORMNUM.thrw(info, frm);

    return Str.get(df.format(info, it, pic));
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @param tp input type
   * @return string
   * @throws QueryException query exception
   */
  private Item formatDate(final Type tp, final QueryContext ctx) throws QueryException {
    final Item it = expr[0].item(ctx, info);
    final byte[] pic = checkEStr(expr[1], ctx);
    final byte[] lng = expr.length == 5 ? checkEStr(expr[2], ctx) : EMPTY;
    final byte[] cal = expr.length == 5 ? checkEStr(expr[3], ctx) : EMPTY;
    final byte[] plc = expr.length == 5 ? checkEStr(expr[4], ctx) : EMPTY;
    if(it == null) return null;
    final ADate date = (ADate) checkType(it, tp);

    final Formatter form = Formatter.get(lng);
    return Str.get(form.formatDate(date, lng, pic, cal, plc, info));
  }
}
