package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert before primitive.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public void merge(final Update update) {
    final InsertBefore newOne = (InsertBefore) update;
    final ANodeList newInsert = newOne.nodes;
    for(final ANode n : newInsert) nodes.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addInsert(pre, data.parent(pre, data.kind(pre)), insseq);
  }

  @Override
  public void update(final NamePool pool) { }
}
