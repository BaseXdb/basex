package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert before primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class InsertBefore extends NodeCopy {
  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param c node copy
   */
  public InsertBefore(final int p, final Data d, final InputInfo i, final ANodeList c) {
    super(PrimitiveType.INSERTBEFORE, p, d, i, c);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    final InsertBefore newOne = (InsertBefore) p;
    final ANodeList newInsert = newOne.insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    l.addInsert(targetPre, data.parent(targetPre, data.kind(targetPre)), insseq, false);
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
