package org.basex.core.cmd;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Evaluates the 'jobs stop' command.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsResult extends Command {
  /**
   * Default constructor.
   * @param id id
   */
  public JobsResult(final String id) {
    super(Perm.ADMIN, id);
  }

  @Override
  protected boolean run() {
    final String id = args[0];
    final JobPool jobs = context.jobs;
    final Map<String, JobResult> results = jobs.results;
    final JobResult result = results.get(id);
    if(result == null) return error(JOBS_UNKNOWN_X.desc, id);
    if(result.value == null && result.exception == null) error(JOBS_RUNNING_X.desc, id);

    try {
      if(result.value == null) throw result.exception;

      final Serializer ser = Serializer.get(out);
      final Iter ir = result.value.iter();
      for(Item it; (it = ir.next()) != null;) {
        ser.serialize(it);
        checkStop();
      }
      return true;
    } catch(final QueryException | IOException ex) {
      exception = ex;
      return error(Util.message(ex));
    } finally {
      results.remove(id);
    }
  }

  @Override
  public void databases(final LockResult lr) {
    // No locks needed
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.JOBS + " " + CmdJobs.RESULT).args();
  }
}
