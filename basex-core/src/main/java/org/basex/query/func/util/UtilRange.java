package org.basex.query.func.util;

import org.basex.query.func.fn.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class UtilRange extends FnSubsequence {
  @Override
  public final long start(final double first) {
    return (long) Math.ceil(first);
  }

  @Override
  public final long end(final long first, final double second) {
    return (long) Math.floor(second);
  }
}
