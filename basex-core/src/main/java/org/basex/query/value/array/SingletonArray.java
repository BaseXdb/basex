package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Array with a single member.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SingletonArray extends XQArray {
  /** Single member. */
  final Value member;

  /**
   * Constructor.
   * @param member member
   */
  SingletonArray(final Value member) {
    super(1, ArrayType.get(member.seqType()));
    this.member = member;
  }

  @Override
  public Value memberAt(final long index) {
    return member;
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    return get(value);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return member.atomValue(qc, ii);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    return this;
  }

  @Override
  public XQArray insertMember(final long pos, final Value value, final QueryContext qc) {
    final Value first = pos == 0 ? value : member, second = pos == 0 ? member : value;
    final ArrayType at = (ArrayType) type.union(ArrayType.get(value.seqType()));
    return new ArrayBuilder(qc, 2).add(first).add(second).array(at);
  }

  @Override
  public XQArray removeMember(final long pos, final QueryContext qc) {
    return empty();
  }

  @Override
  public Iter items() throws QueryException {
    return member.iter();
  }
}
