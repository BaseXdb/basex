package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Delete primitive.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class DeleteNode extends NodeUpdate {
  /** States if the deletion of the target node T is part of a replaceElementContent
   * call on the parent of T, see {@link ReplaceValue}. */
  public final boolean rec;

  /**
   * Constructor.
   * @param pre target node PRE value
   * @param data target data reference
   * @param info input info (can be {@code null})
   */
  public DeleteNode(final int pre, final Data data, final InputInfo info) {
    this(pre, data, info, false);
  }

  /**
   * Constructor for a DELETE primitive that is a product of a replaceElementContent
   * substitution.
   * @param pre target node PRE value
   * @param data target data reference
   * @param info input info (can be {@code null})
   * @param rec this DELETE is a product of a replaceElementContent substitution
   */
  DeleteNode(final int pre, final Data data, final InputInfo info, final boolean rec) {
    super(UpdateType.DELETENODE, pre, data, info);
    this.rec = rec;
  }

  @Override
  public void prepare(final MemData memData, final QueryContext qc) { }

  @Override
  public void merge(final Update update) {
    /* Multiple delete primitives can operate on the same
     * target node, see XQUF. */
  }

  @Override
  public void update(final NamePool pool) {
    pool.remove(new DBNode(data, pre));
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + node() + ']';
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addDelete(pre);
  }
}
