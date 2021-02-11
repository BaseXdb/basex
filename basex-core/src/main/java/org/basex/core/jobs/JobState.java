package org.basex.core.jobs;

/**
 * Job state.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum JobState {
  /** Scheduled. */
  SCHEDULED,
  /** Queued. */
  QUEUED,
  /** Running. */
  RUNNING,
  /** Stopped. */
  STOPPED,
  /** Timeout. */
  TIMEOUT,
  /** Memory. */
  MEMORY,
  /** Cached. */
  CACHED
}
