package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Insert before primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class InsertBefore extends InsertBase {
  /** Parent of node to be inserted. Need to cache this as delete and replace
   * primitives (which are executed before insert before) mess with parent
   * values.
   */
  private final int par;
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
    par = d.parent(p, d.kind(p));
  }

  @Override
  public void apply() {
    super.apply();
    data.insert(pre, par, md);
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
