package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Insert into and insert into as last primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class InsertInto extends NodeCopy {
  /** Insert into as last primitive. If false this is a simple 'insert into' primitive.
   * Basically the same, but important for the order of the insertion sequence. Nodes
   * added with 'insert into as last' must be guaranteed to come last. */
  final boolean last;
  /** States if the insertion on this target node T is part of a replaceElementContent
   * substitution on T, see {@link ReplaceValue}. */
  public final boolean rec;

  /**
   * Constructor for an insertInto which is part of a replaceElementContent substitution.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   * @param l insert into as last
   * @param r part of replaceElementContent substitution
   */
  public InsertInto(final int p, final Data d, final InputInfo i, final ANodeList n,
      final boolean l, final boolean r) {
    super(PrimitiveType.INSERTINTO, p, d, i, n);
    last = l;
    rec = r;
  }

  /**
   * Constructor.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   * @param l insert into as last
   */
  public InsertInto(final int p, final Data d, final InputInfo i, final ANodeList n,
      final boolean l) {
    super(PrimitiveType.INSERTINTO, p, d, i, n);
    last = l;
    rec = false;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    // Make sure insertion sequences are not merged if one of both is a substitution.
    if(rec || ((InsertInto) p).rec) return;

    final InsertInto i = (InsertInto) p;
    final ANodeList newInsert = i.insert;
    if(i.last) {
      // insertion sequence is added after all 'insertInto' sequences for
      // 'insertIntoAsLast' ...
      for(int j = 0; j < newInsert.size(); j++)
        insert.add(newInsert.get(j));
    } else {
      // and v.v. for 'insertInto' it is added before all 'insertIntoAsLast'
      // As we can't add nodes to the beginning of the list we just add them the other
      // way round and switch lists afterwards
      for(int j = 0; j < insert.size(); j++)
        newInsert.add(insert.get(j));
      insert = newInsert;
    }
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
