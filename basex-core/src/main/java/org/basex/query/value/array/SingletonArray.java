package org.basex.query.value.array;

import org.basex.core.jobs.*;
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
  private final Value member;

  /**
   * Constructor.
   * @param member member
   */
  SingletonArray(final Value member) {
    super(ArrayType.get(member.seqType()));
    this.member = member;
  }

  @Override
  public Value memberAt(final long index) {
    return member;
  }

  @Override
  public Value items(final QueryContext qc) {
    return member;
  }

  @Override
  public Iter itemsIter() {
    return member.iter();
  }

  @Override
  public long structSize() {
    return 1;
  }

  @Override
  public XQArray putMember(final long pos, final Value value, final Job job) {
    return get(value);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final Job job) {
    throw Util.notExpected();
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return member.atomValue(qc, ii);
  }

  @Override
  public XQArray reverseArray(final Job job) {
    return this;
  }

  @Override
  public XQArray insertMember(final long pos, final Value value, final Job job) {
    final Value first = pos == 0 ? value : member, second = pos == 0 ? member : value;
    final ArrayType at = (ArrayType) type.union(ArrayType.get(value.seqType()));
    return new ArrayBuilder(job, 2).add(first).add(second).array(at);
  }

  @Override
  public XQArray removeMember(final long pos, final Job job) {
    return empty();
  }
}
