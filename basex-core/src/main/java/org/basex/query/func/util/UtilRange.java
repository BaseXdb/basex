package org.basex.query.func.util;

import org.basex.query.func.fn.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilRange extends FnSubsequence {
  @Override
  public long start(final double first) {
    return (long) Math.ceil(first);
  }

  @Override
  public long end(final long first, final double second) {
    return (long) Math.floor(second);
  }
}
