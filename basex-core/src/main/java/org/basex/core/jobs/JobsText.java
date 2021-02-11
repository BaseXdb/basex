package org.basex.core.jobs;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the jobs management.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface JobsText {
  /** Jobs. */
  byte[] JOBS = token("jobs");
  /** Job. */
  byte[] JOB = token("job");
  /** ID. */
  byte[] ID = token("id");
  /** Running. */
  byte[] DURATION = token("duration");
  /** Type. */
  byte[] TYPE = token("type");
  /** State. */
  byte[] STATE = token("state");
  /** Next start. */
  byte[] START = token("start");
  /** End. */
  byte[] END = token("end");
  /** Interval. */
  byte[] INTERVAL = token("interval");
  /** User. */
  byte[] USER = token("user");
  /** Read locks. */
  byte[] READS = token("reads");
  /** Write locks. */
  byte[] WRITES = token("writes");
  /** Time of registering. */
  byte[] TIME = token("time");
}
