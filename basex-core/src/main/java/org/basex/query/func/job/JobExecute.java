package org.basex.query.func.job;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JobExecute extends JobEval {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final IOContent input = toContent(arg(0), qc);
    final JobOptions options = new JobOptions();
    options.set(JobOptions.CACHE, true);

    final String id = eval(input, options, qc).toJava();

    // wait for result; stop process if child process is stopped
    final JobPool pool = qc.context.jobs;
    try {
      while(true) {
        if(!pool.tasks.containsKey(id)) {
          final Job job = pool.active.get(id);
          if(job == null) break;
          if(job.state == JobState.STOPPED) throw new JobException(Text.INTERRUPTED);
        }
        Performance.sleep(1);
        qc.checkStop();
      }
    } catch(final JobException ex) {
      // stop child process if process is stopped
      pool.remove(id);
      throw ex;
    }

    final QueryJobResult result = pool.results.remove(id);
    return result != null ? result.get() : Empty.VALUE;
  }
}
