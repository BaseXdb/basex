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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNFormat extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<FormatParser> formats = new TokenObjMap<>();

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNFormat(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case FORMAT_INTEGER:  return formatInteger(qc);
      case FORMAT_NUMBER:   return formatNumber(qc);
      case FORMAT_DATETIME: return formatDate(AtomType.DTM, qc);
      case FORMAT_DATE:     return formatDate(AtomType.DAT, qc);
      case FORMAT_TIME:     return formatDate(AtomType.TIM, qc);
      default:              return super.item(qc, ii);
    }
  }

  /**
   * Returns a formatted integer.
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatInteger(final QueryContext qc) throws QueryException {
    final byte[] pic = checkStr(exprs[1], qc);
    final byte[] lng = exprs.length == 2 ? EMPTY : checkStr(exprs[2], qc);

    final Item it = exprs[0].item(qc, info);
    if(it == null) return Str.ZERO;
    final long num = checkItr(it);

    FormatParser fp = formats.get(pic);
    if(fp == null) {
      fp = new IntFormat(pic, info);
      formats.put(pic, fp);
    }
    return Str.get(Formatter.get(lng).formatInt(num, fp));
  }

  /**
   * Returns a formatted number.
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatNumber(final QueryContext qc) throws QueryException {
    // evaluate arguments
    Item it = exprs[0].item(qc, info);
    if(it == null) it = Dbl.NAN;
    else if(!it.type.isNumberOrUntyped()) throw numberError(this, it);
    // retrieve picture
    final byte[] pic = checkStr(exprs[1], qc);
    // retrieve format declaration
    final QNm frm = exprs.length == 3 ? new QNm(trim(checkEStr(exprs[2], qc)), sc) :
      new QNm(EMPTY);
    final DecFormatter df = sc.decFormats.get(frm.id());
    if(df == null) throw FORMNUM.get(info, frm);

    return Str.get(df.format(info, it, pic));
  }

  /**
   * Returns a formatted number.
   * @param qc query context
   * @param tp input type
   * @return string
   * @throws QueryException query exception
   */
  private Item formatDate(final Type tp, final QueryContext qc) throws QueryException {
    final Item it = exprs[0].item(qc, info);
    final byte[] pic = checkEStr(exprs[1], qc);
    final byte[] lng = exprs.length == 5 ? checkEStr(exprs[2], qc) : EMPTY;
    final byte[] cal = exprs.length == 5 ? checkEStr(exprs[3], qc) : EMPTY;
    final byte[] plc = exprs.length == 5 ? checkEStr(exprs[4], qc) : EMPTY;
    if(it == null) return null;
    final ADate date = (ADate) checkType(it, tp);

    final Formatter form = Formatter.get(lng);
    return Str.get(form.formatDate(date, lng, pic, cal, plc, info));
  }
}
