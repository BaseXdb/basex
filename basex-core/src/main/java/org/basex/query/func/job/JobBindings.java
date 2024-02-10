package org.basex.query.func.job;

import java.util.Map.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JobBindings extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String id = toString(arg(0), qc);
    final JobPool jobs = qc.context.jobs;

    Job job = jobs.active.get(id);
    final QueryJobResult jr = jobs.results.get(id);
    if(job == null && jr != null) job = jr.job;
    final QueryJobTask jt = jobs.tasks.get(id);
    if(job == null && jt != null) job = jt.job;

    final MapBuilder mb = new MapBuilder(info);
    if(job instanceof QueryJob) {
      for(final Entry<String, Value> entry : ((QueryJob) job).bindings().entrySet()) {
        mb.put(entry.getKey(), entry.getValue());
      }
    }
    return mb.map();
  }
}
