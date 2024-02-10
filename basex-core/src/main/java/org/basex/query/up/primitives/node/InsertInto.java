package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.up.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert into primitive.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Lukas Kircher
 */
public class InsertInto extends NodeCopy {
  /**
   * Constructor for an insertInto.
   * @param pre target pre value
   * @param data target data instance
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  public InsertInto(final int pre, final Data data, final InputInfo info, final ANodeList nodes) {
    super(UpdateType.INSERTINTO, pre, data, info, nodes);
  }

  @Override
  public void merge(final Update update) {
    final ANodeList newInsert = ((NodeCopy) update).nodes;
    for(final ANode node : newInsert) nodes.add(node);
  }

  @Override
  public final void addAtomics(final AtomicUpdateCache auc) {
    final int s = data.size(pre, data.kind(pre));
    auc.addInsert(pre + s, pre, insseq);
  }

  @Override
  public final void update(final NamePool pool) {
  }
}
