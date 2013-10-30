package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert attribute primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class InsertAttribute extends NodeCopy {
  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param c node copy
   */
  public InsertAttribute(final int p, final Data d, final InputInfo i,
      final ANodeList c) {
    super(PrimitiveType.INSERTATTR, p, d, i, c);
  }

  @Override
  public void update(final NamePool pool) {
    if(insseq == null) return;
    add(pool);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    final ANodeList newInsert = ((NodeCopy) p).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    l.addInsert(targetPre + 1, targetPre, insseq, true);
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }
}
