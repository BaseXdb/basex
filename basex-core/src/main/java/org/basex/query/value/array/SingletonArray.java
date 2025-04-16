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
  public XQArray prepend(final Value head) {
    return new SmallArray(new Value[] { head, member }, union(head));
  }

  @Override
  public XQArray append(final Value last) {
    return new SmallArray(new Value[] { member, last }, union(last));
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    return singleton(value);
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
  public XQArray insertBefore(final long pos, final Value value, final QueryContext qc) {
    return pos == 0 ? prepend(value) : append(value);
  }

  @Override
  public XQArray remove(final long pos, final QueryContext qc) {
    return empty();
  }

  @Override
  public Iter items() throws QueryException {
    return member.iter();
  }
}
