package org.basex.core.jobs;

/**
 * Job state.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public enum JobState {
  /** OK. */
  OK,
  /** Stopped. */
  STOPPED,
  /** Timeout. */
  TIMEOUT,
  /** Memory. */
  MEMORY;
}
