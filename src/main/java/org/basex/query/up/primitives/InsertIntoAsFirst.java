package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Insert into as first primitive.
 *
 * @author BaseX Team 2005-12, BSD License
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
    super(PrimitiveType.INSERTINTOFIRST, p, d, i, c);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    final ANodeList newInsert = ((InsertIntoAsFirst) p).insert;
    for(int j = 0; j < newInsert.size(); j++)
      insert.add(newInsert.get(j));
  }

  @Override
  public void addAtomics(final AtomicUpdateList l) {
    l.addInsert(targetPre + data.attSize(targetPre, data.kind(targetPre)), targetPre,
        insseq, false);
  }

  @Override
  public UpdatePrimitive[] substitute() {
    return new UpdatePrimitive[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
