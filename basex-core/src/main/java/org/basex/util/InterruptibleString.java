package org.basex.util;

import org.basex.core.*;
import org.basex.core.jobs.*;

/**
 * Interruptible string implementation.
 * Inspired by https://stackoverflow.com/questions/910740/cancelling-a-long-running-regex-match
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author gojomo
 */
public class InterruptibleString implements CharSequence {
  /** String. */
  private final String string;

  /**
   * Constructor.
   * @param string string
   */
  public InterruptibleString(final String string) {
    this.string = string;
  }

  @Override
  public char charAt(final int index) {
    checkStop();
    return string.charAt(index);
  }

  @Override
  public int length() {
    return string.length();
  }

  @Override
  public InterruptibleString subSequence(final int start, final int end) {
    return new InterruptibleString(string.substring(start, end));
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
