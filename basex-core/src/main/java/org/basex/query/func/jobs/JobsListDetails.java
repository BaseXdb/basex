package org.basex.query.func.jobs;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.core.cmd.JobsList;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
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
  /** Next start. */
  private static final byte[] START = token("start");
  /** End. */
  private static final byte[] END = token("end");
  /** User. */
  private static final byte[] USER = token("user");
  /** Read locks. */
  private static final byte[] READS = token("reads");
  /** Write locks. */
  private static final byte[] WRITES = token("writes");

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final Context ctx = qc.context;
    final TokenList ids = exprs.length == 0 ? JobsList.ids(ctx) :
      new TokenList(1).add(toToken(exprs[0], qc));

    final int max = ctx.soptions.get(StaticOptions.LOGMSGMAXLEN);
    final JobPool jobs = ctx.jobs;
    final ValueBuilder vb = new ValueBuilder();

    final byte[][] atts = { ID, TYPE, STATE, USER, DURATION, START, END, READS, WRITES };
    for(final byte[] key : ids) {
      final TokenList entry = JobsList.entry(key, jobs, max);
      if(entry == null) continue;

      final FElem elem = new FElem(JOB);
      final int al = atts.length;
      for(int a = 0; a < al; a++) {
        final byte[] value = entry.get(a);
        if(value.length != 0) elem.add(atts[a], value);
      }
      elem.add(entry.get(entry.size() - 1));
      vb.add(elem);
    }
    return vb.value();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
