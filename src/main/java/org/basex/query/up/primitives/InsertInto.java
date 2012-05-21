package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Insert into and insert into as last primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class InsertInto extends InsertBase {
  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param nc node copy
   * @param l insert into as last
   */
  public InsertInto(final int p, final Data d, final InputInfo i,
      final NodeSeqBuilder nc, final boolean l) {
    super(l ? PrimitiveType.INSERTINTOLAST :
      PrimitiveType.INSERTINTO, p, d, i, nc);
  }

  @Override
  public void apply() {
    super.apply();

    final int loc = pre + data.size(pre, data.kind(pre));
    data.insert(loc, pre, md);
  }

  @Override
  public boolean adjacentTexts(final int c) {
    /* No adjacent text nodes possible if nothing has been
     * inserted by this primitive
     */
    if(md.meta.size == 0) return false;

    // take pre value shifts into account after updates on the preceding,
    // preceding sibling and ancestor axis
    final int p = pre + c;
    final int affectedPre = p + data.size(p, data.kind(p)) - md.meta.size;
    boolean merged = false;
    if(md.kind(0) == Data.TEXT)
      merged = mergeTexts(data, affectedPre - 1, affectedPre);
    if(!merged && md.kind(md.meta.size - 1) == Data.TEXT) {
      final int f = affectedPre + md.meta.size;
      merged = mergeTexts(data, f - 1, f);
    }

    return merged;
  }
}
