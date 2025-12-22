package org.basex.query.value.array;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Array with single-item members.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ItemArray extends XQArray {
  /** Members. */
  private Value members;

  /**
   * Constructor.
   * @param members members
   */
  ItemArray(final Value members) {
    super(ArrayType.get(members.type.seqType()));
    this.members = members;
  }

  @Override
  public Value memberAt(final long index) {
    return members.itemAt(index);
  }

  @Override
  public Iter itemsIter() {
    return members.iter();
  }

  @Override
  public Value items(final QueryContext qc) {
    return members;
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return members.atomValue(qc, ii);
  }

  @Override
  public long structSize() {
    return members.size();
  }

  @Override
  public XQArray putMember(final long pos, final Value value, final QueryContext qc) {
    return toTree(qc).putMember(pos, value, qc);
  }

  @Override
  public XQArray insertMember(final long pos, final Value value, final QueryContext qc) {
    return toTree(qc).insertMember(pos, value, qc);
  }

  @Override
  public XQArray removeMember(final long pos, final QueryContext qc) {
    return structSize() == 2 ? get(members.itemAt(pos == 0 ? 1 : 0)) :
      new ItemArray(members.removeItem(pos, qc));
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    return new ItemArray(members.subsequence(pos, length, qc));
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return new ItemArray(members.reverse(qc));
  }

  /**
   * Creates a tree-based version of this array.
   * @param qc query context
   * @return array
   */
  private XQArray toTree(final QueryContext qc) {
    final ArrayBuilder ab = new ArrayBuilder(qc, Long.MIN_VALUE);
    for(final Item member : members) ab.add(member);
    return ab.array((ArrayType) type);
  }

  @Override
  public Item shrink(final QueryContext qc) throws QueryException {
    members = members.shrink(qc);
    type = ArrayType.get(members.seqType().with(Occ.EXACTLY_ONE));
    return this;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, ENTRIES, structSize()), members);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("array { ");
    members.toString(qs);
    qs.token(" }");
  }
}
