package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert into as last primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class InsertIntoAsLast extends NodeCopy {
  /**
   * Constructor for an insertInto which is part of a replaceElementContent substitution.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   */
  public InsertIntoAsLast(final int p, final Data d, final InputInfo i, final ANodeList n) {
    super(UpdateType.INSERTINTOLAST, p, d, i, n);
  }

  @Override
  public void merge(final Update up) {
    final ANodeList newInsert = ((NodeCopy) up).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    final int s = data.size(pre, data.kind(pre));
    l.addInsert(pre + s, pre, insseq, false);
  }

  @Override
  public NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
