package org.basex.query.func.fn;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Date/time functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class DateTime extends StandardFunc {

  /**
   * Checks if the specified item is a Duration item. If it is untyped,
   * a duration is returned.
   * @param it item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  protected Dur checkDur(final Item it) throws QueryException {
    if(it instanceof Dur) return (Dur) it;
    if(it.type.isUntyped()) return new Dur(it.string(info), info);
    throw castError(info, it, AtomType.DUR);
  }

  /**
   * Checks if the specified item has the specified Date type.
   * If it is item, the specified Date is returned.
   * @param it item to be checked
   * @param t target type
   * @param qc query context
   * @return date
   * @throws QueryException query exception
   */
  protected ADate checkDate(final Item it, final AtomType t, final QueryContext qc)
      throws QueryException {
    return (ADate) (it.type.isUntyped() ? t.cast(it, qc, sc, info) : checkType(it, t));
  }

  /**
   * Returns the timezone.
   * @param it input item
   * @return timezone
   */
  protected static DTDur zon(final ADate it) {
    final int tz = it.zon();
    return tz == Short.MAX_VALUE ? null : new DTDur(0, tz);
  }

  /**
   * Adjusts a Time item to the specified time zone.
   * @param it item
   * @param t target type
   * @param qc query context
   * @return duration
   * @throws QueryException query exception
   */
  protected ADate adjust(final Item it, final AtomType t, final QueryContext qc)
      throws QueryException {

    final ADate ad;
    if(it.type.isUntyped()) {
      ad = (ADate) t.cast(it, qc, sc, info);
    } else {
      // clone item
      final ADate a = (ADate) checkType(it, t);
      ad = t == AtomType.TIM ? new Tim(a) : t == AtomType.DAT ? new Dat(a) : new Dtm(a);
    }
    final boolean spec = exprs.length == 2;
    final Item zon = spec ? exprs[1].atomItem(qc, info) : null;
    ad.timeZone(zon == null ? null : (DTDur) checkType(zon, AtomType.DTD), spec, info);
    return ad;
  }
}
