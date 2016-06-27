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

  /**
   * Returns the id of the root job.
   * @return id
   */
  public String id() {
    if(id == null) {
      jobId = Math.max(0, jobId + 1);
      id = "Job-" + jobId;
    }
    return id;
  }
}
