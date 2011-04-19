package org.basex.query.up.primitives;

import org.basex.query.item.ANode;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Abstract base class for all insert into primitives.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class InsertBase extends NodeCopy {

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param nc insertion sequence
   */
  protected InsertBase(final InputInfo ii, final ANode n, final NodeCache nc) {
    super(ii, n, nc);
  }

  @Override
  public void merge(final Primitive p) {
    insert.add(((NodeCopy) p).insert.get(0));
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + node + ", " + insert + "]";
  }
}
