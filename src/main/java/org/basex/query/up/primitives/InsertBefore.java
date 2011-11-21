package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;

/**
 * Insert before primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertBefore extends InsertBase {
  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param c node copy
   */
  public InsertBefore(final int p, final Data d, final InputInfo i,
      final NodeCache c) {
    super(PrimitiveType.INSERTBEFORE, p, d, i, c);
  }

  @Override
  public void apply() {
    super.apply();
    data.insert(pre, data.parent(pre, data.kind(pre)), md);
  }

  @Override
  public boolean adjacentTexts(final int c) {
    final int p = pre + c;
    boolean merged = false;
    final int mds = md.meta.size;
    if(md.kind(0) == Data.TEXT)
      merged = mergeTexts(data, p - 1, p);
    if(!merged && md.kind(mds - 1) == Data.TEXT)
      merged |= mergeTexts(data, p + mds - 1, p + mds);

    return merged;
  }
}
