package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Insert into primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class InsertInto extends NodeCopy {
  /** States if the insertion on this target node T is part of a replaceElementContent
   * substitution on T, see {@link ReplaceValue}. */
  public final boolean rec;

  /**
   * Constructor for an insertInto which is part of a replaceElementContent substitution.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   * @param r part of replaceElementContent substitution
   */
  public InsertInto(final int p, final Data d, final InputInfo i, final ANodeList n,
      final boolean r) {
    super(PrimitiveType.INSERTINTO, p, d, i, n);
    rec = r;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    // Make sure insertion sequences are not merged if one of both is a substitution.
    if(rec || ((InsertInto) p).rec) return;

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
  public void update(final NamePool pool) { }
}
