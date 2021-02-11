package org.basex.core.jobs;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Cached job result.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryJobResult {
  /** Job. */
  public final Job job;
  /** Query result. */
  public Value value;
  /** Exception. */
  public QueryException exception;
  /** Evaluation time (ns). */
  public long time;

  /**
   * Job.
   * @param job job
   */
  public QueryJobResult(final Job job) {
    this.job = job;
  }

  /**
   * Checks if the query result has been cached.
   * @return result of check
   */
  public boolean cached() {
    return job.state == JobState.CACHED;
  }
}
