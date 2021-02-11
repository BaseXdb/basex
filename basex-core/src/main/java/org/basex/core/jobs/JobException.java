package org.basex.core.jobs;

/**
 * This exception is called whenever a job is interrupted.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JobException extends RuntimeException {
  /**
   * Constructor.
   * @param message error message
   */
  public JobException(final String message) {
    super(message);
  }
}
