package org.basex.query;

import static org.basex.util.Token.*;

import java.math.*;
import java.time.*;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Date time properties.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryDateTime {
  /** Current Date. */
  public Dat date;
  /** Current DateTime. */
  public Dtm datm;
  /** Current Time. */
  public Tim time;
  /** Current timezone. */
  public DTDur zone;
  /** Current nanoseconds. */
  public long nano;

  /**
   * Constructor.
   * @throws QueryException query exception
   */
  public QueryDateTime() throws QueryException {
    final ZonedDateTime zdt = ZonedDateTime.now();
    final String ymd = DateTime.DATE.format(zdt), hms = DateTime.TIME.format(zdt);
    final String zon = DateTime.ZONE.format(zdt);
    time = new Tim(token(hms + zon), null);
    date = new Dat(token(ymd + zon), null);
    datm = new Dtm(token(ymd + 'T' + hms + zon), BasicType.DATE_TIME_STAMP, null);
    zone = new DTDur(BigDecimal.valueOf(zdt.getOffset().getTotalSeconds()));
    nano = System.nanoTime();
  }
}
