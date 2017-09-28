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
  public long start(final double v) {
    return (long) Math.ceil(v);
  }

  @Override
  public long length(final double v) {
    return (long) Math.floor(v);
  }

  @Override
  public long length(final long start, final long len) {
    return len == Long.MAX_VALUE ? len : len - start + 1;
  }
}
