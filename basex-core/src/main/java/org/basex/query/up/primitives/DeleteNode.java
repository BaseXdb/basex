package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Delete primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class DeleteNode extends UpdatePrimitive {
  /** States if the deletion of the target node T is part of a replaceElementContent
   * call on the parent of T, see {@link ReplaceValue}. */
  public final boolean rec;

  /**
   * Constructor.
   * @param p target node PRE value
   * @param d target data reference
   * @param i input info
   */
  public DeleteNode(final int p, final Data d, final InputInfo i) {
    this(p, d, i, false);
  }

  /**
   * Constructor for a delete primitive that is a product of a replaceElementContent
   * substitution.
   * @param p target node PRE value
   * @param d target data reference
   * @param i input info
   * @param r this delete is a product of a replaceElementContent substitution
   */
  public DeleteNode(final int p, final Data d, final InputInfo i, final boolean r) {
    super(PrimitiveType.DELETENODE, p, d, i);
    rec = r;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    /* Multiple delete primitives can operate on the same
     * target node, see XQUF. */
  }

  @Override
  public void update(final NamePool pool) {
    pool.remove(new DBNode(data, targetPre));
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + getTargetNode() + ']';
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    l.addDelete(targetPre);
  }
}
