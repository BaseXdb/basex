package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A small array that is stored in a single Java array.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class SmallArray extends TreeArray {
  /** The members. */
  final Value[] members;

  /**
   * Constructor.
   * @param members members
   * @param type type
   */
  SmallArray(final Value[] members, final Type type) {
    super(type);
    this.members = members;
    assert members.length >= 2 && members.length <= MAX_SMALL;
  }

  @Override
  public Value memberAt(final long index) {
    return members[(int) index];
  }

  @Override
  public long structSize() {
    return members.length;
  }

  @Override
  public XQArray put(final long pos, final Value value) {
    final Value[] values = members.clone();
    values[(int) pos] = value;
    return new SmallArray(values, union(value));
  }

  @Override
  public XQArray insertMember(final long pos, final Value value, final QueryContext qc) {
    qc.checkStop();
    final Type tp = union(value);
    final int ml = members.length, p = (int) pos;
    final Value[] out = new Value[ml + 1];
    Array.copy(members, p, out);
    out[p] = value;
    Array.copy(members, p, ml - p, out, p + 1);

    return ml < MAX_SMALL ? new SmallArray(out, tp) :
      new BigArray(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, ml + 1), tp);
  }

  @Override
  public XQArray removeMember(final long pos, final QueryContext qc) {
    qc.checkStop();
    final int ml = members.length;
    if(ml == 2) return get(members[pos == 0 ? 1 : 0]);

    final int p = (int) pos;
    final Value[] out = new Value[ml - 1];
    Array.copy(members, p, out);
    Array.copy(members, p + 1, ml - 1 - p, out, p);
    return new SmallArray(out, type);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();
    final int p = (int) pos, l = (int) length;
    return new SmallArray(slice(members, p, p + l), type);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    final int ml = members.length;
    final Value[] tmp = new Value[ml];
    for(int m = 0; m < ml; m++) tmp[m] = members[ml - 1 - m];
    return new SmallArray(tmp, type);
  }
}
