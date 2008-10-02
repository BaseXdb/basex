package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import java.math.BigDecimal;
import java.util.Calendar;

import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DTd;
import org.basex.query.xquery.item.Dat;
import org.basex.query.xquery.item.Date;
import org.basex.query.xquery.item.Dec;
import org.basex.query.xquery.item.Dtm;
import org.basex.query.xquery.item.Dur;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Tim;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;

/**
 * Date functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNDate extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Item it = arg[0].atomic(this, true);
    if(it == null) return Iter.EMPTY;

    switch(func) {
      case YEADUR: return yea(checkDur(it));
      case YEADTM: return yea(checkDate(it, Type.DTM, ctx));
      case YEADAT: return yea(checkDate(it, Type.DAT, ctx));
      case MONDUR: return mon(checkDur(it));
      case MONDTM: return mon(checkDate(it, Type.DTM, ctx));
      case MONDAT: return mon(checkDate(it, Type.DAT, ctx));
      case DAYDUR: return day(checkDur(it));
      case DAYDTM: return day(checkDate(it, Type.DTM, ctx));
      case DAYDAT: return day(checkDate(it, Type.DAT, ctx));
      case HOUDUR: return hou(checkDur(it));
      case HOUDTM: return hou(checkDate(it, Type.DTM, ctx));
      case HOUTIM: return hou(checkDate(it, Type.TIM, ctx));
      case MINDUR: return min(checkDur(it));
      case MINDTM: return min(checkDate(it, Type.DTM, ctx));
      case MINTIM: return min(checkDate(it, Type.TIM, ctx));
      case SECDUR: return sec(checkDur(it));
      case SECDTM: return sec(checkDate(it, Type.DTM, ctx));
      case SECTIM: return sec(checkDate(it, Type.TIM, ctx));
      case ZONDTM: return zon(checkDate(it, Type.DTM, ctx));
      case ZONDAT: return zon(checkDate(it, Type.DAT, ctx));
      case ZONTIM: return zon(checkDate(it, Type.TIM, ctx));
      case DATZON: return datzon(it, arg.length == 1 ? null : arg[1]);
      case DTMZON: return dtmzon(it, arg.length == 1 ? null : arg[1]);
      case TIMZON: return timzon(it, arg.length == 1 ? null : arg[1]);
      case DATETIME: return dattim(it, arg[1]);
      default: BaseX.notexpected(func); return null;
    }
  }

  /**
   * Returns the years of the specified date.
   * @param it date
   * @return time.
   */
  private Iter yea(final Item it) {
    return Itr.iter(it instanceof Dur ? ((Dur) it).yea() :
      ((Date) it).xc.getYear());
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return time.
   */
  private Iter mon(final Item it) {
    return Itr.iter(it instanceof Dur ? ((Dur) it).mon() :
      ((Date) it).xc.getMonth());
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return time.
   */
  private Iter day(final Item it) {
    return Itr.iter(it instanceof Dur ? (int) ((Dur) it).day() :
      ((Date) it).xc.getDay());
  }

  /**
   * Returns the hours of the specified date.
   * @param it date
   * @return time.
   */
  private Iter hou(final Item it) {
    return Itr.iter(it instanceof Dur ? (int) ((Dur) it).hou() :
      ((Date) it).xc.getHour());
  }

  /**
   * Returns the minutes of the specified date.
   * @param it date
   * @return time.
   */
  private Iter min(final Item it) {
    return Itr.iter(it instanceof Dur ? ((Dur) it).min() :
      ((Date) it).xc.getMinute());
  }

  /**
   * Returns the seconds of the specified date.
   * @param it date
   * @return time.
   */
  private Iter sec(final Item it) {
    if(it instanceof Dur) return Dec.iter(((Dur) it).sec().doubleValue());
    final int s = ((Date) it).xc.getSecond();
    final BigDecimal d = ((Date) it).xc.getFractionalSecond();
    return Dec.iter(s + (d != null ? d.doubleValue() : 0));
  }

  /**
   * Returns the timezone.
   * @param it input item
   * @return timezone
   */
  private Iter zon(final Item it) {
    final int z = ((Date) it).xc.getTimezone();
    return z == UNDEF ? Iter.EMPTY : new DTd(z).iter();
  }

  /**
   * Checks if the specified item has the specified type.
   * If it's an untyped item, the specified type is returned.
   * @param it item to be checked
   * @param t target type
   * @param ctx xquery context
   * @return date
   * @throws XQException evaluation exception
   */
  private Item checkDate(final Item it, final Type t, final XQContext ctx)
      throws XQException {
    return it.u() ? t.e(it, ctx) : check(it, t);
  }

  /**
   * Checks if the specified item is a duration. If it's an untyped item,
   * a duration is returned.
   * @param it item to be checked
   * @return duration
   * @throws XQException evaluation exception
   */
  private Item checkDur(final Item it) throws XQException {
    if(it.u()) return new Dur(it.str());
    if(!it.d()) Err.type(info(), Type.DUR, it);
    return it;
  }

  /**
   * Adjusts the Date to the time zone.
   * @param it item to be checked
   * @param zon timezone
   * @return duration
   * @throws XQException evaluation exception
   */
  private Iter datzon(final Item it, final Iter zon) throws XQException {
    final Item i = it.u() ? new Dat(it.str()) : check(it, Type.DAT);
    return adjust((Date) i, zon).iter();
  }

  /**
   * Adjusts the DateTime to the time zone.
   * @param it item to be checked
   * @param zon timezone
   * @return duration
   * @throws XQException evaluation exception
   */
  private Iter dtmzon(final Item it, final Iter zon) throws XQException {
    final Item i = it.u() ? new Dtm(it.str()) : check(it, Type.DTM);
    return adjust((Date) i, zon).iter();
  }

  /**
   * Adjusts the Time to the time zone.
   * @param it item to be checked
   * @param zon timezone
   * @return duration
   * @throws XQException evaluation exception
   */
  private Iter timzon(final Item it, final Iter zon) throws XQException {
    final Item i = it.u() ? new Tim(it.str()) : check(it, Type.TIM);
    return adjust((Date) i, zon).iter();
  }

  /**
   * Returns a DateTime.
   * @param date item to be checked
   * @param zon time zone
   * @return duration
   * @throws XQException evaluation exception
   */
  private Iter dattim(final Item date, final Iter zon) throws XQException {
    final Item time = zon.atomic(this, true);
    if(time == null) return Iter.EMPTY;

    final Item d = date.u() ? new Dat(date.str()) : date;
    final Item t = time.u() ? new Tim(time.str()) : time;

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
    return dtm.iter();
  }

  /**
   * Adjusts the timezone.
   * @param date input date
   * @param z timezone
   * @return adjusted date
   * @throws XQException evaluation exception
   */
  private Date adjust(final Date date, final Iter z) throws XQException {
    final Item zon = z != null ? z.next() : null;
    if(z != null && zon == null) {
      date.xc.setTimezone(UNDEF);
      return date;
    }

    final int zn = date.xc.getTimezone();
    int tz = 0;
    if(zon == null) {
      final Calendar c = Calendar.getInstance();
      tz = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000;
    } else {
      final DTd dtd = (DTd) check(zon, Type.DTD);;
      tz = (int) (dtd.min() + dtd.hou() * 60);
      if(dtd.sec().signum() != 0 || Math.abs(tz) > 840) Err.or(INVALZONE, zon);
    }
    if(zn != UNDEF) date.xc.add(Date.df.newDuration(-60000L * (zn - tz)));
    date.xc.setTimezone(tz);
    return date;
  }
}
