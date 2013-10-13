package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert after primitive.
 *
 * @author BaseX Team 2005-13, BSD License
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
    super(PrimitiveType.INSERTAFTER, p, d, i, c);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    final ANodeList newInsert = ((InsertAfter) p).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateList l) {
    final int k = data.kind(targetPre);
    final int s = data.size(targetPre, k);
    l.addInsert(targetPre + s, data.parent(targetPre, k), insseq, false);
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
