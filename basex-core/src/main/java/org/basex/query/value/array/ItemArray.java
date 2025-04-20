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
  final Value members;

  /**
   * Constructor.
   * @param members members
   */
  ItemArray(final Value members) {
    super(members.size(), ArrayType.get(members.type.seqType()));
    this.members = members;
  }

  @Override
  public Value memberAt(final long index) {
    return members.itemAt(index);
  }

  @Override
  public XQArray prepend(final Value head) {
    return toTree().prepend(head);
  }

  @Override
  public XQArray append(final Value last) {
    return toTree().append(last);
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    return toTree().put(pos, value);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    return new ItemArray(members.subsequence(pos, length, qc));
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return members.atomValue(qc, ii);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return new ItemArray(members.reverse(qc));
  }

  @Override
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    return toTree().insertBefore(pos, value, qc);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    return toTree().remove(pos, qc);
  }

  @Override
  public Iter items() throws QueryException {
    return members.iter();
  }

  /**
   * Creates a tree representation of this array.
   * @return array
   */
  private XQArray toTree() {
    final ArrayBuilder ab = new ArrayBuilder();
    for(final Item member : members) ab.add(member);
    return ab.array((ArrayType) type);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, ENTRIES, size), members);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("array { ");
    members.toString(qs);
    qs.token(" }");
  }
}
