package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Insert attribute primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertAttribute extends InsertBase {

  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param c node copy
   */
  public InsertAttribute(final int p, final Data d,
      final InputInfo i, final NodeCache c) {
    super(PrimitiveType.INSERTATTR, p, d, i, c);
  }

  @Override
  public void apply() {
    super.apply();
    data.insertAttr(pre + 1, pre, md);
  }

  @Override
  public void update(final NamePool pool) {
    if(md == null) return;
    add(pool);
  }

  @Override
  public boolean checkTextAdjacency(final int c) {
    return false;
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + getTargetDBNode() + ", " + insert + "]";
  }
}
