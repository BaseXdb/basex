package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Date/time functions.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
abstract class DateTime extends StandardFunc {
  /**
   * Checks if the specified item is a Duration item. If it is untyped,
   * a duration is returned.
   * @param item item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  protected Dur checkDur(final Item item) throws QueryException {
    if(item instanceof Dur) return (Dur) item;
    if(item.type.isUntyped()) return new Dur(item.string(info), info);
    throw typeError(item, AtomType.DUR, info);
  }

  /**
   * Checks if the specified item has the specified Date type.
   * If it is item, the specified Date is returned.
   * @param item item to be checked
   * @param type target type
   * @param qc query context
   * @return date
   * @throws QueryException query exception
   */
  protected ADate checkDate(final Item item, final AtomType type, final QueryContext qc)
      throws QueryException {
    return (ADate) (item.type.isUntyped() ? type.cast(item, qc, sc, info) : checkType(item, type));
  }

  /**
   * Returns the timezone.
   * @param it input item
   * @return timezone or {@link Empty#VALUE}
   */
  protected static Item zon(final ADate it) {
    return it.hasTz() ? new DTDur(0, it.tz()) : Empty.VALUE;
  }

  /**
   * Adjusts a Time item to the specified time zone.
   * @param item item
   * @param type target type
   * @param qc query context
   * @return duration
   * @throws QueryException query exception
   */
  protected ADate adjust(final Item item, final AtomType type, final QueryContext qc)
      throws QueryException {

    final ADate ad;
    if(item.type.isUntyped()) {
      ad = (ADate) type.cast(item, qc, sc, info);
    } else {
      // clone item
      final ADate a = (ADate) checkType(item, type);
      ad = type == AtomType.TIM ? new Tim(a) : type == AtomType.DAT ? new Dat(a) : new Dtm(a);
    }
    final boolean spec = exprs.length == 2;
    final Item zon = spec ? exprs[1].atomItem(qc, info) : Empty.VALUE;
    ad.timeZone(zon == Empty.VALUE ? null : (DTDur) checkType(zon, AtomType.DTD), spec, info);
    return ad;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
