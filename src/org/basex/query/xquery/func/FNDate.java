package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
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
import org.basex.util.Token;

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
      default: throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Returns the years of the specified date.
   * @param it date
   * @return time.
   */
  private Iter yea(final Item it) {
    final Date d = (Date) it;
    final long l = d.mon / 12;
    return Itr.iter(d.minus ? -l : l);
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return time.
   */
  private Iter mon(final Item it) {
    final Date d = (Date) it;
    final int t = d.mon % 12;
    return finish(d, d.d() ? t : t + 1);
  }

  /**
   * Returns the months of the specified date.
   * @param it date
   * @return time.
   */
  private Iter day(final Item it) {
    final Date d = (Date) it;
    final long t = d.sec / 86400;
    return finish(d, d.d() ? t : t + 1);
  }

  /**
   * Returns the hours of the specified date.
   * @param it date
   * @return time.
   */
  private Iter hou(final Item it) {
    final Date d = (Date) it;
    return finish(d, d.sec % 86400 / 3600);
  }

  /**
   * Returns the minutes of the specified date.
   * @param it date
   * @return time.
   */
  private Iter min(final Item it) {
    final Date d = (Date) it;
    return finish(d, d.sec % 3600 / 60);
  }

  /**
   * Returns the seconds of the specified date.
   * @param it date
   * @return time.
   */
  private Iter sec(final Item it) {
    final Date d = (Date) it;
    final BigDecimal b = BigDecimal.valueOf(d.sec % 60).add(
        BigDecimal.valueOf(d.mil));
    return Dec.iter(d.minus ? b.negate() : b);
  }

  /**
   * Returns the timezone.
   * @param it input item
   * @return timezone
   */
  private Iter zon(final Item it) {
    final Date date = (Date) it;
    return !date.zone ? Iter.EMPTY : new DTd(date.zshift).iter();
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
   * Adds the sign to the specified date.
   * @param d date
   * @param l value
   * @return date.
   */
  private Iter finish(final Date d, final long l) {
    return d.d() ? Itr.iter(d.minus ? -l : l) : Itr.iter(l);
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
    final Date v = adjust((Date) i, zon);
    v.sec -= v.sec % 86400;
    return v.iter();
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
    final Date v = adjust((Date) i, zon);
    v.sec = v.sec % 86400;
    if(v.sec < 0) v.sec += 86400;
    return v.iter();
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
    dtm.sec += tim.sec;
    dtm.mil = dtm.mil;

    if(dtm.zone && tim.zone && dtm.zshift != tim.zshift)
      Err.or(FUNZONE, dtm, tim);
    if(!dtm.zone) {
      dtm.zone = tim.zone;
      dtm.zshift = tim.zshift;
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
    Item zon = z != null ? z.next() : null;
    if(z != null && zon == null) {
      date.zone = false;
      date.zshift = 0;
      return date;
    }

    final java.util.Date d = Calendar.getInstance().getTime();
    final byte[] zone = Token.token(new SimpleDateFormat("Z").format(d));
    final int cshift = Token.toInt(Token.substring(zone, 0, 3)) * 60 +
      Token.toInt(Token.substring(zone, 3));

    if(zon == null) {
      if(date.zone) date.sec -= (date.zshift - cshift) * 60;
      date.zone = true;
      date.zshift = cshift;
    } else {
      check(zon, Type.DTD);
      final DTd dtd = (DTd) zon;
      if(dtd.sec % 60 != 0 || dtd.mil != 0 || dtd.sec > 50400)
        Err.or(INVALZONE, zon);

      final long nsec = dtd.minus ? -dtd.sec : dtd.sec;
      if(date.zone) date.sec -= date.zshift * 60 - nsec;
      date.zshift = (int) (nsec / 60);
      date.zone  = true;
    }
    // correct day overflow
    if(date.sec / 86400 > Date.dpm(date.mon / 12, date.mon % 12)) {
      date.sec %= 86400;
      date.mon++;
    }
    if(date.sec < 0) {
      date.sec += 86400;
      if(--date.mon >= 0 && date.sec < 86400) {
        date.sec += Date.dpm(date.mon / 12, date.mon % 12) * 86400;
      }
    }
    return date;
  }
}
