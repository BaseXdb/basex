package org.basex.core.jobs;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Job context.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class JobContext {
  /** Query id. */
  private static long jobId = -1;

  /** Performance measurements. */
  public Performance performance;
  /** Listener, watching for information. */
  public InfoListener listener;

  /** Job id. Will be set via if job is being registered. */
  String id;
  /** Reference to root job. */
  Job job;

  /**
   * Constructor.
   * @param job root job
   */
  JobContext(final Job job) {
    this.job = job;
  }

  /**
   * Returns the id of the root job.
   * @return id
   */
  public String id() {
    if(id == null) id(Util.className(job));
    return id;
  }

  /**
   * Sets the job id, composed by the specified name and an incremental id.
   * @param name name
   */
  public synchronized void id(final String name) {
    if(id != null) throw Util.notExpected("Name of job cannot be assigned twice.");
    jobId = Math.max(0, jobId + 1);
    id = name + '-' + jobId;
  }
}
