package org.basex.query;

import org.basex.query.value.*;

/**
 * Query focus: context value, position, size.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class QueryFocus {
  /** Context value. */
  public Value value;
  /** Context position. */
  public long pos = 1;
  /** Context size. */
  public long size = 1;
}
