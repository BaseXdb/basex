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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class JobListDetails extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Context ctx = qc.context;
    final TokenList ids = exprs.length > 0 ? new TokenList(1).add(toToken(exprs[0], qc)) :
      qc.context.jobs.ids();

    final int max = ctx.soptions.get(StaticOptions.LOGMSGMAXLEN);
    final JobPool jobs = ctx.jobs;
    final ValueBuilder vb = new ValueBuilder(qc);

    for(final byte[] id : ids) {
      final String key = Token.string(id);
      Job job = jobs.active.get(key);
      final QueryJobResult jr = jobs.results.get(key);
      if(job == null && jr != null) job = jr.job;
      final QueryJobTask jt = jobs.tasks.get(key);
      if(job == null && jt != null) job = jt.job;
      if(job == null) continue;

      final JobContext jc = job.jc();
      final long ms = jc.performance != null
          ? jc.performance.ns(false) / 1000000 : jr != null
          ? jr.time / 1000000 : -1;

      final FElem elem = new FElem(JOB);
      elem.add(ID, key);
      elem.add(TYPE, jc.type());
      elem.add(STATE, job.state.name().toLowerCase(Locale.ENGLISH));
      elem.add(USER, jc.context.clientName());
      if(ms >= 0) elem.add(DURATION, DTDur.get(ms).string(info));
      if(jt != null) {
        elem.add(START, dateTime(jt.start));
        if(jt.end != Long.MAX_VALUE) elem.add(END, dateTime(jt.end));
        if(jt.interval != 0) elem.add(INTERVAL, DTDur.get(jt.interval).string(info));
      }
      elem.add(READS, jc.locks.reads.toString());
      elem.add(WRITES, jc.locks.writes.toString());
      elem.add(TIME, dateTime(jc.time));
      elem.add(Token.chop(Token.normalize(Token.token(jc.toString())), max));
      vb.add(elem);
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
    dtm.timeZone(tz, false, info);
    return dtm.string(info);
  }
}
