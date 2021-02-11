package org.basex.query;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Date time properties.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final Date d = new Date();
    final String ymd = DateTime.format(d, DateTime.DATE);
    final String hms = DateTime.format(d, DateTime.TIME);
    final String zon = DateTime.format(d, DateTime.ZONE);
    final String znm = zon.substring(0, 3), zns = zon.substring(3);
    time = new Tim(token(hms + znm + ':' + zns), null);
    date = new Dat(token(ymd + znm + ':' + zns), null);
    datm = new Dtm(token(ymd + 'T' + hms + znm + ':' + zns), null);
    zone = new DTDur(Strings.toInt(znm), Strings.toInt(zns));
    nano = System.nanoTime();
  }
}
