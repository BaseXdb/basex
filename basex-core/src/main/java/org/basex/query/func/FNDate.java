package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Date functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNDate extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNDate(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].item(ctx, info);
    if(it == null) return null;

    switch(func) {
      case YEARS_FROM_DURATION:
        return Int.get(checkDur(it).yea());
      case YEAR_FROM_DATETIME:
        return Int.get(checkDate(it, AtomType.DTM, ctx).yea());
      case YEAR_FROM_DATE:
        return Int.get(checkDate(it, AtomType.DAT, ctx).yea());
      case MONTHS_FROM_DURATION:
        return Int.get(checkDur(it).mon());
      case MONTH_FROM_DATETIME:
        return Int.get(checkDate(it, AtomType.DTM, ctx).mon());
      case MONTH_FROM_DATE:
        return Int.get(checkDate(it, AtomType.DAT, ctx).mon());
      case DAYS_FROM_DURATION:
        return Int.get(checkDur(it).day());
      case DAY_FROM_DATETIME:
        return Int.get(checkDate(it, AtomType.DTM, ctx).day());
      case DAY_FROM_DATE:
        return Int.get(checkDate(it, AtomType.DAT, ctx).day());
      case HOURS_FROM_DURATION:
        return Int.get(checkDur(it).hou());
      case HOURS_FROM_DATETIME:
        return Int.get(checkDate(it, AtomType.DTM, ctx).hou());
      case HOURS_FROM_TIME:
        return Int.get(checkDate(it, AtomType.TIM, ctx).hou());
      case MINUTES_FROM_DURATION:
        return Int.get(checkDur(it).min());
      case MINUTES_FROM_DATETIME:
        return Int.get(checkDate(it, AtomType.DTM, ctx).min());
      case MINUTES_FROM_TIME:
        return Int.get(checkDate(it, AtomType.TIM, ctx).min());
      case SECONDS_FROM_DURATION:
        return Dec.get(checkDur(it).sec());
      case SECONDS_FROM_DATETIME:
        return Dec.get(checkDate(it, AtomType.DTM, ctx).sec());
      case SECONDS_FROM_TIME:
        return Dec.get(checkDate(it, AtomType.TIM, ctx).sec());
      case TIMEZONE_FROM_DATETIME:
        return zon(checkDate(it, AtomType.DTM, ctx));
      case TIMEZONE_FROM_DATE:
        return zon(checkDate(it, AtomType.DAT, ctx));
      case TIMEZONE_FROM_TIME:
        return zon(checkDate(it, AtomType.TIM, ctx));
      case ADJUST_DATE_TO_TIMEZONE:
        return adjust(it, AtomType.DAT, ctx);
      case ADJUST_DATETIME_TO_TIMEZONE:
        return adjust(it, AtomType.DTM, ctx);
      case ADJUST_TIME_TO_TIMEZONE:
        return adjust(it, AtomType.TIM, ctx);
      case DATETIME:
        return dateTime(it, ctx);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Returns the timezone.
   * @param it input item
   * @return timezone
   */
  private static DTDur zon(final ADate it) {
    final int tz = it.zon();
    return tz == Short.MAX_VALUE ? null : new DTDur(0, tz);
  }

  /**
   * Checks if the specified item has the specified Date type.
   * If it is item, the specified Date is returned.
   * @param it item to be checked
   * @param t target type
   * @param ctx query context
   * @return date
   * @throws QueryException query exception
   */
  private ADate checkDate(final Item it, final Type t, final QueryContext ctx)
      throws QueryException {
    return (ADate) (it.type.isUntyped() ? t.cast(it, ctx, sc, info) : checkType(it, t));
  }

  /**
   * Checks if the specified item is a Duration item. If it is untyped,
   * a duration is returned.
   * @param it item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  private Dur checkDur(final Item it) throws QueryException {
    if(it instanceof Dur) return (Dur) it;
    if(it.type.isUntyped()) return new Dur(it.string(info), info);
    throw Err.typeError(this, AtomType.DUR, it);
  }

  /**
   * Returns a DateTime item.
   * @param date item to be checked
   * @param ctx query context
   * @return duration
   * @throws QueryException query exception
   */
  private ADate dateTime(final Item date, final QueryContext ctx) throws QueryException {
    final Item zon = exprs.length == 2 ? exprs[1].item(ctx, info) : null;
    if(zon == null) return null;
    final Dat d = date.type.isUntyped() ? new Dat(date.string(info), info) :
      (Dat) checkType(date, AtomType.DAT);
    final Tim t = zon.type.isUntyped() ? new Tim(zon.string(info), info) :
      (Tim) checkType(zon, AtomType.TIM);
    return new Dtm(d, t, info);
  }

  /**
   * Adjusts a Time item to the specified time zone.
   * @param it item
   * @param t target type
   * @param ctx query context
   * @return duration
   * @throws QueryException query exception
   */
  private ADate adjust(final Item it, final Type t, final QueryContext ctx) throws QueryException {
    final ADate ad;
    if(it.type.isUntyped()) {
      ad = (ADate) t.cast(it, ctx, sc, info);
    } else {
      // clone item
      final ADate a = (ADate) checkType(it, t);
      ad = t == AtomType.TIM ? new Tim(a) : t == AtomType.DAT ? new Dat(a) : new Dtm(a);
    }
    final boolean spec = exprs.length == 2;
    final Item zon = spec ? exprs[1].item(ctx, info) : null;
    ad.timeZone(zon == null ? null : (DTDur) checkType(zon, AtomType.DTD), spec, info);
    return ad;
  }
}
