package org.basex.query.iter;

import org.basex.query.item.ANode;

/**
 * Axis iterator interface, throwing no exceptions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class AxisIter extends NodeIter {
  @Override
  public abstract ANode next();
}
