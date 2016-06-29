package org.basex.core.jobs;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Job context.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobContext {
  /** Query id. */
  private static long jobId = -1;

  /** Performance measurements. */
  public Performance performance;
  /** Listener, watching for information. */
  public InfoListener listener;
  /** Database context. */
  public Context context;

  /** Root job. */
  private final Job job;

  /** Job id. Will be set via if job is being registered. */
  private String id;
  /** Job name. */
  private String tp;

  /**
   * Constructor.
   * @param job job
   */
  JobContext(final Job job) {
    this.job = job;
  }

  /**
   * Returns the id of the root job.
   * @return id
   */
  public String id() {
    if(id == null) {
      jobId = Math.max(0, jobId + 1);
      id = "job" + jobId;
    }
    return id;
  }

  /**
   * Sets a job type.
   * @param type type
   */
  public void type(final String type) {
    tp = type;
  }

  /**
   * Returns the job type.
   * @return name
   */
  public String type() {
    return tp != null ? tp : Util.className(job);
  }

  @Override
  public String toString() {
    return job.toString();
  }
}
