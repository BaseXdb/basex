package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Delete primitive.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param ii input info
   */
  public DeleteNode(final int pre, final Data data, final InputInfo ii) {
    this(pre, data, ii, false);
  }

  /**
   * Constructor for a delete primitive that is a product of a replaceElementContent
   * substitution.
   * @param pre target node PRE value
   * @param data target data reference
   * @param ii input info
   * @param rec this delete is a product of a replaceElementContent substitution
   */
  public DeleteNode(final int pre, final Data data, final InputInfo ii, final boolean rec) {
    super(UpdateType.DELETENODE, pre, data, ii);
    this.rec = rec;
  }

  @Override
  public void prepare(final MemData tmp) { }

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
