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
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class InsertInto extends NodeCopy {
  /**
   * Constructor for an insertInto.
   * @param pre target pre value
   * @param data target data instance
   * @param ii input info
   * @param nodes node copy insertion sequence
   */
  public InsertInto(final int pre, final Data data, final InputInfo ii, final ANodeList nodes) {
    super(UpdateType.INSERTINTO, pre, data, ii, nodes);
  }

  @Override
  public final void merge(final Update up) {
    final ANodeList newInsert = ((NodeCopy) up).nodes;
    for(final ANode n : newInsert) nodes.add(n);
  }

  @Override
  public final void addAtomics(final AtomicUpdateCache l) {
    final int s = data.size(pre, data.kind(pre));
    l.addInsert(pre + s, pre, insseq);
  }

  @Override
  public final NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }

  @Override
  public final void update(final NamePool pool) { }
}
