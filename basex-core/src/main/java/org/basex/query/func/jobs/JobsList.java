package org.basex.query.func.jobs;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
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
public final class JobsList extends JobsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final TokenList list = new TokenList();
    final Map<String, Job> active = qc.context.jobs.active;
    final Set<String> set = active.keySet();

    for(final String id : set) list.add(id);
    for(final String id : qc.context.jobs.tasks.keySet()) {
      if(!set.contains(id)) list.add(id);
    }
    return StrSeq.get(sort(list));
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
