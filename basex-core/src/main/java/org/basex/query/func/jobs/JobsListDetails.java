package org.basex.query.func.jobs;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsListDetails extends JobsFn {
  /** Job. */
  private static final byte[] JOB = token("job");
  /** ID. */
  private static final byte[] ID = token("id");
  /** Running. */
  private static final byte[] DURATION = token("duration");
  /** Type. */
  private static final byte[] TYPE = token("type");
  /** State. */
  private static final byte[] STATE = token("state");
  /** Next start. */
  private static final byte[] START = token("start");
  /** User. */
  private static final byte[] USER = token("user");

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final JobPool jobs = qc.context.jobs;
    final Set<String> set = new HashSet<>();
    if(exprs.length == 0) {
      for(final String id : jobs.active.keySet()) set.add(id);
      for(final String id : jobs.tasks.keySet()) set.add(id);
      for(final String id : jobs.results.keySet()) set.add(id);
    } else {
      set.add(Token.string(toToken(exprs[0], qc)));
    }
    final TokenList list = new TokenList(set.size());
    for(final String id : set) list.add(id);
    sort(list);

    final int ml = qc.context.soptions.get(StaticOptions.LOGMSGMAXLEN);
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] key : list) {
      final String id = string(key);
      // ignore jobs that have already finished
      Job job = jobs.active.get(id);
      JobTask jt = jobs.tasks.get(id);
      JobResult jr = jobs.results.get(id);
      if(job == null && jr != null) job = jr.job;
      if(job == null && jt != null) job = jt.job;
      if(job == null) continue;

      final JobContext jc = job.job();
      final long ms = jc.performance != null
          ? (System.nanoTime() - jc.performance.start()) / 1000000 : jr != null
          ? jr.time / 1000000 : -1;

      final FElem elem = new FElem(JOB);
      elem.add(ID, id);
      elem.add(TYPE, jc.type());
      elem.add(STATE, job.state.toString().toLowerCase(Locale.ENGLISH));
      elem.add(USER, jc.context.user().name());
      if(ms >= 0) elem.add(DURATION, new DTDur(ms).string(info));
      if(jt != null) elem.add(START, new Dtm(jt.start, info).string(info));
      elem.add(chop(normalize(token(jc.toString())), ml));
      vb.add(elem);
    }
    return vb.value();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
