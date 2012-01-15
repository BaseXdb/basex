package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;

/**
 * Insert into as first primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class InsertIntoFirst extends InsertBase {
  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param c node copy
   */
  public InsertIntoFirst(final int p, final Data d, final InputInfo i,
      final NodeCache c) {
    super(PrimitiveType.INSERTINTOFIRST, p, d, i, c);
  }

  @Override
  public void apply() {
    super.apply();
    data.insert(pre + data.attSize(pre, data.kind(pre)), pre, md);
  }

  @Override
  public boolean adjacentTexts(final int c) {
    /* Text node adjacency can only occur at the end of the insertion sequence
     * as this is inserted before all other siblings - no left sibling to merge
     * with.
     */
    if(md.kind(md.meta.size - 1) != Data.TEXT) return false;

    // take pre value shifts into account
    final int p = pre + c;
    final int loc = p + data.attSize(p, data.kind(p)) + md.meta.size - 1;
    return mergeTexts(data, loc , loc + 1);
  }
}
