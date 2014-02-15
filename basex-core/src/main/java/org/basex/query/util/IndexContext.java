package org.basex.query.util;

import org.basex.data.*;

/**
 * This class contains data required for index operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IndexContext {
  /** Data reference. */
  public final Data data;
  /** Flag for iterative evaluation. */
  public final boolean iterable;

  /**
   * Constructor.
   * @param d data reference
   * @param i iterable flag
   */
  public IndexContext(final Data d, final boolean i) {
    data = d;
    iterable = i;
  }
}
