package org.basex.query.func;

import static org.basex.query.util.Err.*;
import java.math.BigDecimal;
import java.util.Calendar;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Date;
import org.basex.query.item.Dec;
import org.basex.query.item.Dtm;
import org.basex.query.item.Dur;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.AtomType;
import org.basex.query.item.Tim;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Date functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNDate extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNDate(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = expr[0].item(ctx, input);
    if(it == null) return null;
    final boolean d = expr.length == 2;
    final Item zon = d ? expr[1].item(ctx, input) : null;

    switch(sig) {
      case YEARS_FROM_DURATION:
        return yea(checkDur(it));
      case YEAR_FROM_DATETIME:
        return yea(checkDate(it, AtomType.DTM, ctx));
      case YEAR_FROM_DATE:
        return yea(checkDate(it, AtomType.DAT, ctx));
      case MONTHS_FROM_DURATION:
        return mon(checkDur(it));
      case MONTH_FROM_DATETIME:
        return mon(checkDate(it, AtomType.DTM, ctx));
      case MONTH_FROM_DATE:
        return mon(checkDate(it, AtomType.DAT, ctx));
      case DAYS_FROM_DURATION:
        return day(checkDur(it));
      case DAY_FROM_DATETIME:
        return day(checkDate(it, AtomType.DTM, ctx));
      case DAY_FROM_DATE:
        return day(checkDate(it, AtomType.DAT, ctx));
      case HOURS_FROM_DURATION:
        return hou(checkDur(it));
      case HOURS_FROM_DATETIME:
        return hou(checkDate(it, AtomType.DTM, ctx));
      case HOURS_FROM_TIME:
        return hou(checkDate(it, AtomType.TIM, ctx));
      case MINUTES_FROM_DURATION:
        return min(checkDur(it));
      case MINUTES_FROM_DATETIME:
        return min(checkDate(it, AtomType.DTM, ctx));
      case MINUTES_FROM_TIME:
        return min(checkDate(it, AtomType.TIM, ctx));
      case SECONDS_FROM_DURATION:
        return sec(checkDur(it));
      case SECONDS_FROM_DATETIME:
        return sec(checkDate(it, AtomType.DTM, ctx));
      case SECONDS_FROM_TIME:
        return sec(checkDate(it, AtomType.TIM, ctx));
      case TIMEZONE_FROM_DATETIME:
        return zon(checkDate(it, AtomType.DTM, ctx));
      case TIMEZONE_FROM_DATE:
        return zon(checkDate(it, AtomType.DAT, ctx));
      case TIMEZONE_FROM_TIME:
        return zon(checkDate(it, AtomType.TIM, ctx));
      case ADJUST_DATE_TO_TIMEZONE:
        return datzon(it, zon, d);
      case ADJUST_DATETIME_TO_TIMEZONE:
        return dtmzon(it, zon, d);
      case ADJUST_TIME_TO_TIMEZONE:
        return timzon(it, zon, d);
      case DATETIME:
        return dattim(it, zon);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Returns the years of the specified date.
   * @param it date
   * @return years
   */
  private Item yea(final Item it) {
    return Int.get(it instanceof Dur ? ((Dur) it).yea() :
      ((Date) it).xc.getYear());
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return months
   */
  private Item mon(final Item it) {
    return Int.get(it instanceof Dur ? ((Dur) it).mon() :
      ((Date) it).xc.getMonth());
  }

  /**
   * Returns the days of the specified date.
   * @param it date
   * @return days
   */
  private Item day(final Item it) {
    return Int.get(it instanceof Dur ? (int) ((Dur) it).day() :
      ((Date) it).xc.getDay());
  }

  /**
   * Returns the hours of the specified date.
   * @param it date
   * @return hours
   */
  private Item hou(final Item it) {
    return Int.get(it instanceof Dur ? (int) ((Dur) it).hou() :
      ((Date) it).xc.getHour());
  }

  /**
   * Returns the minutes of the specified date.
   * @param it date
   * @return minutes
   */
  private Item min(final Item it) {
    return Int.get(it instanceof Dur ? ((Dur) it).min() :
      ((Date) it).xc.getMinute());
  }

  /**
   * Returns the seconds of the specified date.
   * @param it date
   * @return seconds
   */
  private Item sec(final Item it) {
    if(it instanceof Dur) return Dec.get(((Dur) it).sec().doubleValue());
    final int s = ((Date) it).xc.getSecond();
    final BigDecimal d = ((Date) it).xc.getFractionalSecond();
    return Dec.get(s + (d != null ? d.doubleValue() : 0));
  }

  /**
   * Returns the timezone.
   * @param it input item
   * @return timezone
   */
  private Item zon(final Item it) {
    final int z = ((Date) it).xc.getTimezone();
    return z == Item.UNDEF ? null : new DTd(z);
  }

  /**
   * Checks if the specified item has the specified type.
   * If it's an untyped item, the specified type is returned.
   * @param it item to be checked
   * @param t target type
   * @param ctx query context
   * @return date
   * @throws QueryException query exception
   */
  private Item checkDate(final Item it, final Type t, final QueryContext ctx)
      throws QueryException {
    return it.type.isUntyped() ? t.cast(it, ctx, input) : checkType(it, t);
  }

  /**
   * Checks if the specified item is a duration. If it's an untyped item,
   * a duration is returned.
   * @param it item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  private Item checkDur(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.isUntyped()) return new Dur(it.string(input), input);
    if(!ip.isDuration()) Err.type(this, AtomType.DUR, it);
    return it;
  }

  /**
   * Adjusts the Date to the time zone.
   * @param it item to be checked
   * @param zon timezone
   * @param d zone was defined
   * @return duration
   * @throws QueryException query exception
   */
  private Item datzon(final Item it, final Item zon, final boolean d)
      throws QueryException {

    final Item i = it.type.isUntyped() ? new Dat(it.string(input), input) :
      checkType(it, AtomType.DAT);
    return adjust((Date) i, zon, d);
  }

  /**
   * Adjusts the DateTime to the time zone.
   * @param it item to be checked
   * @param zon timezone
   * @param d zone was defined
   * @return duration
   * @throws QueryException query exception
   */
  private Item dtmzon(final Item it, final Item zon, final boolean d)
      throws QueryException {

    final Item i = it.type.isUntyped() ? new Dtm(it.string(input), input) :
      checkType(it, AtomType.DTM);
    return adjust((Date) i, zon, d);
  }

  /**
   * Adjusts the Time to the time zone.
   * @param it item to be checked
   * @param zon timezone
   * @param d zone was defined
   * @return duration
   * @throws QueryException query exception
   */
  private Item timzon(final Item it, final Item zon, final boolean d)
      throws QueryException {

    final Item i = it.type.isUntyped() ? new Tim(it.string(input), input) :
      checkType(it, AtomType.TIM);
    return adjust((Date) i, zon, d);
  }

  /**
   * Returns a DateTime.
   * @param date item to be checked
   * @param tm time zone
   * @return duration
   * @throws QueryException query exception
   */
  private Item dattim(final Item date, final Item tm) throws QueryException {
    if(tm == null) return null;

    final Item d = date.type.isUntyped() ?
        new Dat(date.string(input), input) : date;
    final Item t = tm.type.isUntyped() ?
        new Tim(tm.string(input), input) : tm;

    final Dtm dtm = new Dtm((Dat) checkType(d, AtomType.DAT));
    final Tim tim = (Tim) checkType(t, AtomType.TIM);

    dtm.xc.setTime(tim.xc.getHour(), tim.xc.getMinute(), tim.xc.getSecond(),
        tim.xc.getMillisecond());

    final int zone = tim.xc.getTimezone();
    if(dtm.xc.getTimezone() == Item.UNDEF) {
      dtm.xc.setTimezone(zone);
    } else if(dtm.xc.getTimezone() != zone && zone != Item.UNDEF) {
      FUNZONE.thrw(input, dtm, tim);
    }
    return dtm;
  }

  /**
   * Adjusts the timezone.
   * @param date input date
   * @param zon timezone
   * @param d zone was specified
   * @return adjusted date
   * @throws QueryException query exception
   */
  private Date adjust(final Date date, final Item zon, final boolean d)
      throws QueryException {

    if(d && zon == null) {
      date.xc.setTimezone(Item.UNDEF);
      return date;
    }

    final int zn = date.xc.getTimezone();
    int tz = 0;
    if(zon == null) {
      final Calendar c = Calendar.getInstance();
      tz = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000;
    } else {
      final DTd dtd = (DTd) checkType(zon, AtomType.DTD);
      tz = (int) (dtd.min() + dtd.hou() * 60);
      if(dtd.sec().signum() != 0 || Math.abs(tz) > 840) {
        INVALZONE.thrw(input, zon);
      }
    }
    if(zn != Item.UNDEF) date.xc.add(Date.df.newDuration(-60000L * (zn - tz)));
    date.xc.setTimezone(tz);
    return date;
  }
}
