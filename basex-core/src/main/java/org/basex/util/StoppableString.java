package org.basex.util;

import org.basex.core.*;
import org.basex.core.jobs.*;

/**
 * Stoppable string implementation.
 * Inspired by https://stackoverflow.com/questions/910740/cancelling-a-long-running-regex-match
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author gojomo
 */
public final class StoppableString implements CharSequence {
  /** String. */
  private final String string;
  /** Job to be checked (if {@code null}, the thread interruption status is checked). */
  private final Job job;
  /** Number of character accesses. */
  private int count;

  /**
   * Constructor.
   * @param string string
   */
  public StoppableString(final String string) {
    this(string, null);
  }

  /**
   * Constructor.
   * @param string string
   * @param job job to be checked (can be {@code null})
   */
  public StoppableString(final String string, final Job job) {
    this.string = string;
    this.job = job;
  }

  @Override
  public char charAt(final int index) {
    if((++count & 0xFFF) == 0) {
      if(job != null) job.checkStop();
      else checkStop();
    }
    return string.charAt(index);
  }

  @Override
  public int length() {
    return string.length();
  }

  @Override
  public String subSequence(final int start, final int end) {
    return string.substring(start, end);
  }

  @Override
  public String toString() {
    return string;
  }

  /**
   * Checks if search should be interrupted.
   */
  public static void checkStop() {
    if(Thread.interrupted()) throw new JobException(Text.INTERRUPTED);
  }
}
