package org.basex.query.func;

import static org.basex.query.QueryText.*;
import java.math.BigDecimal;
import java.util.Calendar;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Date;
import org.basex.query.item.Dec;
import org.basex.query.item.Dtm;
import org.basex.query.item.Dur;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Tim;
import org.basex.query.item.Type;
import org.basex.query.util.Err;

/**
 * Date functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FNDate extends Fun {
  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    // functions have 1 or 2 arguments...
    final Item it = expr[0].atomic(ctx);
    if(it == null) return null;
    final boolean d = expr.length == 2;
    final Item zon = d ? expr[1].atomic(ctx) : null;

    switch(func) {
      case YEADUR:   return yea(checkDur(it));
      case YEADTM:   return yea(checkDate(it, Type.DTM, ctx));
      case YEADAT:   return yea(checkDate(it, Type.DAT, ctx));
      case MONDUR:   return mon(checkDur(it));
      case MONDTM:   return mon(checkDate(it, Type.DTM, ctx));
      case MONDAT:   return mon(checkDate(it, Type.DAT, ctx));
      case DAYDUR:   return day(checkDur(it));
      case DAYDTM:   return day(checkDate(it, Type.DTM, ctx));
      case DAYDAT:   return day(checkDate(it, Type.DAT, ctx));
      case HOUDUR:   return hou(checkDur(it));
      case HOUDTM:   return hou(checkDate(it, Type.DTM, ctx));
      case HOUTIM:   return hou(checkDate(it, Type.TIM, ctx));
      case MINDUR:   return min(checkDur(it));
      case MINDTM:   return min(checkDate(it, Type.DTM, ctx));
      case MINTIM:   return min(checkDate(it, Type.TIM, ctx));
      case SECDUR:   return sec(checkDur(it));
      case SECDTM:   return sec(checkDate(it, Type.DTM, ctx));
      case SECTIM:   return sec(checkDate(it, Type.TIM, ctx));
      case ZONDTM:   return zon(checkDate(it, Type.DTM, ctx));
      case ZONDAT:   return zon(checkDate(it, Type.DAT, ctx));
      case ZONTIM:   return zon(checkDate(it, Type.TIM, ctx));
      case DATZON:   return datzon(it, zon, d);
      case DTMZON:   return dtmzon(it, zon, d);
      case TIMZON:   return timzon(it, zon, d);
      case DATETIME: return dattim(it, zon);
      default:       return super.atomic(ctx);
    }
  }

  /**
   * Returns the years of the specified date.
   * @param it date
   * @return time.
   */
  private Item yea(final Item it) {
    return Itr.get(it instanceof Dur ? ((Dur) it).yea() :
      ((Date) it).xc.getYear());
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return time.
   */
  private Item mon(final Item it) {
    return Itr.get(it instanceof Dur ? ((Dur) it).mon() :
      ((Date) it).xc.getMonth());
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return time.
   */
  private Item day(final Item it) {
    return Itr.get(it instanceof Dur ? (int) ((Dur) it).day() :
      ((Date) it).xc.getDay());
  }

  /**
   * Returns the hours of the specified date.
   * @param it date
   * @return time.
   */
  private Item hou(final Item it) {
    return Itr.get(it instanceof Dur ? (int) ((Dur) it).hou() :
      ((Date) it).xc.getHour());
  }

  /**
   * Returns the minutes of the specified date.
   * @param it date
   * @return time.
   */
  private Item min(final Item it) {
    return Itr.get(it instanceof Dur ? ((Dur) it).min() :
      ((Date) it).xc.getMinute());
  }

  /**
   * Returns the seconds of the specified date.
   * @param it date
   * @return time.
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
    return z == UNDEF ? null : new DTd(z);
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
    return it.u() ? t.e(it, ctx) : check(it, t);
  }

  /**
   * Checks if the specified item is a duration. If it's an untyped item,
   * a duration is returned.
   * @param it item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  private Item checkDur(final Item it) throws QueryException {
    if(it.u()) return new Dur(it.str());
    if(!it.d()) Err.type(info(), Type.DUR, it);
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

    final Item i = it.u() ? new Dat(it.str()) : check(it, Type.DAT);
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

    final Item i = it.u() ? new Dtm(it.str()) : check(it, Type.DTM);
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

    final Item i = it.u() ? new Tim(it.str()) : check(it, Type.TIM);
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

    final Item d = date.u() ? new Dat(date.str()) : date;
    final Item t = tm.u() ? new Tim(tm.str()) : tm;

    final Dtm dtm = new Dtm((Dat) check(d, Type.DAT));
    final Tim tim = (Tim) check(t, Type.TIM);

    dtm.xc.setTime(tim.xc.getHour(), tim.xc.getMinute(), tim.xc.getSecond(),
        tim.xc.getMillisecond());

    final int zone = tim.xc.getTimezone();
    if(dtm.xc.getTimezone() == UNDEF) {
      dtm.xc.setTimezone(zone);
    } else if(dtm.xc.getTimezone() != zone && zone != UNDEF) {
      Err.or(FUNZONE, dtm, tim);
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
      date.xc.setTimezone(UNDEF);
      return date;
    }

    final int zn = date.xc.getTimezone();
    int tz = 0;
    if(zon == null) {
      final Calendar c = Calendar.getInstance();
      tz = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000;
    } else {
      final DTd dtd = (DTd) check(zon, Type.DTD);
      tz = (int) (dtd.min() + dtd.hou() * 60);
      if(dtd.sec().signum() != 0 || Math.abs(tz) > 840) Err.or(INVALZONE, zon);
    }
    if(zn != UNDEF) date.xc.add(Date.df.newDuration(-60000L * (zn - tz)));
    date.xc.setTimezone(tz);
    return date;
  }
}
