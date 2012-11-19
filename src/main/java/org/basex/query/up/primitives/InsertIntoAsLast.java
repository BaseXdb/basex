package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Insert into as last primitive.
 *
 * @author BaseX Team 2005-12, BSD License
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
  public InsertIntoAsLast(final int p, final Data d, final InputInfo i,
      final ANodeList n) {
    super(PrimitiveType.INSERTINTOLAST, p, d, i, n);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    final ANodeList newInsert = ((NodeCopy) p).insert;
    for(int j = 0; j < newInsert.size(); j++)
      insert.add(newInsert.get(j));
  }

  @Override
  public void addAtomics(final AtomicUpdateList l) {
    final int s = data.size(targetPre, data.kind(targetPre));
    l.addInsert(targetPre + s, targetPre, insseq, false);
  }

  @Override
  public UpdatePrimitive[] substitute() {
    return new UpdatePrimitive[] { this };
  }

  @Override
  public void update(NamePool pool) { }
}
