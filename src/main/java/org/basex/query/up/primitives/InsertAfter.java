package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Insert after primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class InsertAfter extends InsertBase {
  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param c insert copy
   */
  public InsertAfter(final int p, final Data d, final InputInfo i,
      final NodeSeqBuilder c) {
    super(PrimitiveType.INSERTAFTER, p, d, i, c);
  }

  @Override
  public void apply() {
    super.apply();

    final int k = data.kind(pre);
    data.insert(pre + data.size(pre, k), data.parent(pre, k), md);
  }

  @Override
  public boolean adjacentTexts(final int c) {
    final int p = pre + c;
    // size of og target node
    final int ps = data.size(p, data.kind(p));
    final int affectedPre = p + ps;
    boolean merged = false;
    final int mds = md.meta.size;
    if(md.kind(0) == Data.TEXT)
      merged = mergeTexts(data, affectedPre - 1, affectedPre);
    if(!merged && md.kind(mds - 1) == Data.TEXT)
      merged |= mergeTexts(data, affectedPre + mds - 1, affectedPre + mds);

    return merged;
  }
}
