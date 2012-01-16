package org.basex.query.iter;

import org.basex.query.item.ANode;

/**
 * Interface for light-weight axis iterators, throwing no exceptions.
 * <b>Important</b>: if nodes that are returned by this iterator are to be
 * further processed, they need to finalized via {@link ANode#finish()}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AxisIter extends NodeIter {
  @Override
  public abstract ANode next();
}
