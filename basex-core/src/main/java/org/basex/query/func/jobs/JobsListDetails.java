package org.basex.query.func.jobs;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsListDetails extends StandardFunc {
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
  /** State. */
  private static final byte[] USER = token("user");

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final Map<String, Job> jobs = qc.context.jobs.jobs;
    final Set<String> set = exprs.length == 0 ? jobs.keySet() :
      Collections.singleton(Token.string(toToken(exprs[0], qc)));

    final int ml = qc.context.soptions.get(StaticOptions.LOGMSGMAXLEN);
    final ValueBuilder vb = new ValueBuilder();
    for(final String key : set) {
      // skips jobs that finished in between
      final Job job = jobs.get(key);
      if(job == null) continue;

      final JobContext jc = job.job();
      final FElem elem = new FElem(JOB).add(ID, key).add(TYPE, jc.type());
      elem.add(chop(normalize(token(jc.toString())), ml));

      final JobState state;
      if(jc.performance != null) {
        final long ms = (System.nanoTime() - jc.performance.start()) / 1000000;
        elem.add(DURATION, new DTDur(ms).string(info));
        state = job.state;
      } else {
        state = JobState.QUEUED;
      }
      elem.add(STATE, state.toString().toLowerCase(Locale.ENGLISH));
      elem.add(USER, jc.context.user().name());
      vb.add(elem);
    }
    return vb.value();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
