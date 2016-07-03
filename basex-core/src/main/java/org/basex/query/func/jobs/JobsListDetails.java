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

    final Map<String, Job> active = qc.context.jobs.active;
    final Set<String> set = exprs.length == 0 ? active.keySet() :
      Collections.singleton(Token.string(toToken(exprs[0], qc)));

    final int ml = qc.context.soptions.get(StaticOptions.LOGMSGMAXLEN);
    final ValueBuilder vb = new ValueBuilder();
    for(final String key : set) {
      // skips jobs that finished in between
      final Job job = active.get(key);
      if(job == null) continue;

      final JobContext jc = job.job();
      final FElem elem = new FElem(JOB);
      elem.add(ID, key);
      elem.add(TYPE, jc.type());
      if(jc.performance != null) {
        final long ms = (System.nanoTime() - jc.performance.start()) / 1000000;
        elem.add(DURATION, new DTDur(ms).string(info));
      }
      elem.add(STATE, job.state.toString().toLowerCase(Locale.ENGLISH));
      elem.add(USER, jc.context.user().name());
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
