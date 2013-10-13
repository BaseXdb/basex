package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert into primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public class InsertInto extends NodeCopy {
  /**
   * Constructor for an insertInto.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   */
  public InsertInto(final int p, final Data d, final InputInfo i, final ANodeList n) {
    super(PrimitiveType.INSERTINTO, p, d, i, n);
  }

  @Override
  public final void merge(final UpdatePrimitive p) {
    final ANodeList newInsert = ((NodeCopy) p).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public final void addAtomics(final AtomicUpdateList l) {
    final int s = data.size(targetPre, data.kind(targetPre));
    l.addInsert(targetPre + s, targetPre, insseq, false);
  }

  @Override
  public final UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }

  @Override
  public final void update(final NamePool pool) { }
}
