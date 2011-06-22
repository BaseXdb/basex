package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Delete primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class DeleteNode extends StructuralUpdate {

  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   */
  public DeleteNode(final int p, final Data d, final InputInfo i) {
    super(PrimitiveType.DELETENODE, p, d, i);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    /* Multiple delete primitives can operate on the same
     * target node.
     */
  }

  @Override
  public void apply() {
    shifts = -1 * data.size(pre, data.kind(pre));
    data.delete(pre);
  }

  @Override
  public boolean checkTextAdjacency(final int c) {
    // take pre value shifts into account
    final int p = pre + c;
    return mergeTexts(data, p - 1, p);
  }

  @Override
  public void update(final NamePool pool) {
    pool.remove(new DBNode(data, pre));
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + getTargetDBNode() + "]";
  }
}