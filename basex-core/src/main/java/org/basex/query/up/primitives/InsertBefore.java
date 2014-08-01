package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert before primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class InsertBefore extends NodeCopy {
  /**
   * Constructor.
   * @param pre pre
   * @param data data
   * @param ii input info
   * @param nodes node copy insertion sequence
   */
  public InsertBefore(final int pre, final Data data, final InputInfo ii, final ANodeList nodes) {
    super(UpdateType.INSERTBEFORE, pre, data, ii, nodes);
  }

  @Override
  public void merge(final Update up) {
    final InsertBefore newOne = (InsertBefore) up;
    final ANodeList newInsert = newOne.nodes;
    for(final ANode n : newInsert) nodes.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    l.addInsert(pre, data.parent(pre, data.kind(pre)), insseq);
  }

  @Override
  public NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
