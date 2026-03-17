package org.basex.query.func.job;

import static org.basex.core.jobs.JobsText.*;

import java.math.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobListDetails extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Context ctx = qc.context;
    final String id = toStringOrNull(arg(0), qc);

    final TokenList ids = id != null ? new TokenList(1).add(id) : qc.context.jobs.ids();
    final int max = ctx.soptions.get(StaticOptions.LOGMSGMAXLEN);
    final JobPool jobs = ctx.jobs;
    final ValueBuilder vb = new ValueBuilder(qc);

    for(final byte[] k : ids) {
      final String key = Token.string(k);
      Job job = jobs.active.get(key);
      final QueryJobResult jr = jobs.results.get(key);
      if(job == null && jr != null) job = jr.job;
      final QueryJobTask jt = jobs.tasks.get(key);
      if(job == null && jt != null) job = jt.job;
      if(job == null) continue;

      final JobContext jc = job.jc();
      final long ms = jc.performance != null
          ? jc.performance.nanoRuntime(false) / 1000000 : jr != null
          ? jr.time / 1000000 : -1;

      final FBuilder elem = FElem.build(Q_JOB);
      elem.attr(Q_ID, key);
      elem.attr(Q_TYPE, jc.type());
      elem.attr(Q_STATE, job.state.name().toLowerCase(Locale.ENGLISH));
      elem.attr(Q_USER, jc.context.clientName());
      if(ms >= 0) elem.attr(Q_DURATION, DTDur.get(ms).string(info));
      if(jt != null) {
        elem.attr(Q_START, dateTime(jt.start));
        if(jt.end != Long.MAX_VALUE) elem.attr(Q_END, dateTime(jt.end));
        if(jt.interval != 0) elem.attr(Q_INTERVAL, DTDur.get(jt.interval).string(info));
      }
      elem.attr(Q_READS, jc.locks.reads);
      elem.attr(Q_WRITES, jc.locks.writes);
      elem.attr(Q_TIME, dateTime(jc.time));
      elem.text(Token.chop(Token.normalize(Token.token(jc)), max));
      vb.add(elem.finish());
    }
    return vb.value(this);
  }

  /**
   * Returns a timezone-adjusted dateTime representation.
   * @param ms milliseconds since 01/01/1970
   * @return date time
   * @throws QueryException query exception
   */
  private byte[] dateTime(final long ms) throws QueryException {
    final Dtm dtm = Dtm.get(ms);
    final DTDur tz = new DTDur(BigDecimal.valueOf(TimeZone.getDefault().getOffset(ms) / 1000));
    return dtm.timeZone(tz, false, info).string(info);
  }
}
