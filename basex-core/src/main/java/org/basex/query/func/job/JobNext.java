package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.math.*;
import java.time.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobNext extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Cron cron = QueryJob.toCron(toString(arg(0), qc), info);
    final Long cnt = toLongOrNull(arg(1), qc);
    final long count = cnt != null ? cnt : 1;
    if(count < 0) throw JOBS_RANGE_X.get(info, count);

    // query clock: repeated calls in a single query yield the same results
    final LocalDateTime from = qc.dateTime().datm.toJava().toGregorianCalendar().
        toZonedDateTime().toLocalDateTime();
    final ZoneId zone = ZoneId.systemDefault();
    return new Iter() {
      LocalDateTime dt = from;
      long c;

      @Override
      public Item next() throws QueryException {
        // the expression may stop matching before the requested count is reached
        if(dt == null || c++ >= count) return null;
        qc.checkStop();
        dt = cron.next(dt);
        return dt == null ? null : dateTime(dt.atZone(zone));
      }
    };
  }

  /**
   * Converts a zoned date and time to a dateTime item.
   * @param zdt zoned date and time
   * @return dateTime item
   * @throws QueryException query exception
   */
  private Item dateTime(final ZonedDateTime zdt) throws QueryException {
    final DTDur tz = new DTDur(BigDecimal.valueOf(zdt.getOffset().getTotalSeconds()));
    return Dtm.buildUnchecked(BasicType.DATE_TIME, (long) zdt.getYear(),
        (long) zdt.getMonthValue(), (long) zdt.getDayOfMonth(), (long) zdt.getHour(),
        (long) zdt.getMinute(), BigDecimal.valueOf(zdt.getSecond()), tz, info);
  }
}
