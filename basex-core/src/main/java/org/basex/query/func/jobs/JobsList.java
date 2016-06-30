package org.basex.query.func.jobs;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsList extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final Map<String, Job> queued = qc.context.jobs.queued;
    final TokenList list = new TokenList(queued.size());
    for(final String id : queued.keySet()) list.add(id);
    return StrSeq.get(list);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
