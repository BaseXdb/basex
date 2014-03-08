package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert after primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class InsertAfter extends NodeCopy {
  /**
   * Constructor.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param c node copy insertion sequence
   */
  public InsertAfter(final int p, final Data d, final InputInfo i, final ANodeList c) {
    super(UpdateType.INSERTAFTER, p, d, i, c);
  }

  @Override
  public void merge(final Update p) {
    final ANodeList newInsert = ((NodeCopy) p).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    final int k = data.kind(pre);
    final int s = data.size(pre, k);
    l.addInsert(pre + s, data.parent(pre, k), insseq, false);
  }

  @Override
  public NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
