package org.basex.query.func.util;

import org.basex.query.func.fn.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class UtilItemRange extends FnSubsequence {
  @Override
  public long start(final double value) {
    return (long) Math.ceil(value);
  }

  @Override
  public long length(final double value) {
    return (long) Math.floor(value);
  }

  @Override
  public long length(final long start, final long length) {
    return length == Long.MAX_VALUE ? length : length - start + 1;
  }
}
