package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Date/time functions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class DateTime extends StandardFunc {
  /**
   * Checks if the specified item is a Duration item. If it is untyped, a duration is returned.
   * @param item item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  protected final Dur checkDur(final Item item) throws QueryException {
    if(item instanceof Dur) return (Dur) item;
    if(item.type.isUntyped()) return new Dur(item.string(info), info);
    throw typeError(item, DURATION, info);
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
   * Adjusts a date/time item to the specified time zone.
   * @param item item
   * @param type target type
   * @param qc query context
   * @return adjusted item
   * @throws QueryException query exception
   */
  final ADate adjust(final Item item, final AtomType type, final QueryContext qc)
      throws QueryException {

    // clone item
    ADate date = toDate(item, type, qc);
    if(!item.type.isUntyped()) {
      date = type == TIME ? new Tim(date) : type == DATE ? new Dat(date) : new Dtm(date);
    }
    final Item zone = arg(1).atomItem(qc, info);
    final DTDur dur = zone.isEmpty() ? null : (DTDur) checkType(zone, DAY_TIME_DURATION);
    date.timeZone(dur, defined(1) && zone.isEmpty(), info);
    return date;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
