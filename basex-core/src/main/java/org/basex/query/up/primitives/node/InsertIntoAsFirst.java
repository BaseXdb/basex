package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert into as first primitive.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public final class InsertIntoAsFirst extends NodeCopy {
  /**
   * Constructor.
   * @param pre target node pre value
   * @param data target data reference
   * @param ii input info
   * @param nodes insertion sequence node list
   */
  public InsertIntoAsFirst(final int pre, final Data data, final InputInfo ii,
      final ANodeList nodes) {
    super(UpdateType.INSERTINTOFIRST, pre, data, ii, nodes);
  }

  @Override
  public void merge(final Update update) {
    final ANodeList newInsert = ((NodeCopy) update).nodes;
    for(final ANode n : newInsert) nodes.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addInsert(pre + data.attSize(pre, data.kind(pre)), pre, insseq);
  }

  @Override
  public void update(final NamePool pool) { }
}
