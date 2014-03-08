package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert into as first primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class InsertIntoAsFirst extends NodeCopy {
  /**
   * Constructor.
   * @param p target node pre value
   * @param d target data reference
   * @param i input info
   * @param c insertion sequence node list
   */
  public InsertIntoAsFirst(final int p, final Data d, final InputInfo i,
      final ANodeList c) {
    super(UpdateType.INSERTINTOFIRST, p, d, i, c);
  }

  @Override
  public void merge(final Update up) {
    final ANodeList newInsert = ((NodeCopy) up).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    l.addInsert(pre + data.attSize(pre, data.kind(pre)), pre,
        insseq, false);
  }

  @Override
  public NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
