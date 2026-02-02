package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.time.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCivilTimezone extends DateTimeFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Dtm value = (Dtm) checkType(arg(0), BasicType.DATE_TIME, qc);
    final String place = toStringOrNull(arg(1), qc);

    final Instant instant = value.toJava().toGregorianCalendar().toInstant();
    final ZoneId id;
    try {
      id = place != null ? ZoneId.of(place) : ZoneId.systemDefault();
    } catch(final RuntimeException ex) {
      Util.debug(ex);
      throw PLACE_X.get(info, place);
    }

    return DTDur.get(ZonedDateTime.ofInstant(instant, id).getOffset().getTotalSeconds() * 1000L);
  }
}
