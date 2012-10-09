package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Insert attribute primitive.
 *
 * @author BaseX Team 2005-12, BSD License
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
  public void merge(final UpdatePrimitive p) throws QueryException {
    final ANodeList newInsert = ((InsertAttribute) p).insert;
    for(int j = 0; j < newInsert.size(); j++)
      insert.add(newInsert.get(j));
  }

  @Override
  public void addAtomics(final AtomicUpdateList l) {
    l.addInsert(targetPre + 1, targetPre, insseq, true);
  }

  @Override
  public UpdatePrimitive[] substitute() {
    return new UpdatePrimitive[] { this };
  }
}
