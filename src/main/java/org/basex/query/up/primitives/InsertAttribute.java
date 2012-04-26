package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.util.*;

/**
 * Insert attribute primitive.
 *
 * @author BaseX Team 2005-12, BSD License
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
  public boolean adjacentTexts(final int c) {
    return false;
  }
}
