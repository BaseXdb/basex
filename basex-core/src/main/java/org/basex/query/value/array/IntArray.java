package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Int array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntArray extends NativeArray {
  /** Members. */
  final long[] members;

  /**
   * Constructor.
   * @param members members
   */
  public IntArray(final long[] members) {
    super(members.length, AtomType.INTEGER);
    this.members = members;
  }

  @Override
  public Int memberAt(final long index) {
    return Int.get(members[(int) index]);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    final int sz = (int) size;
    final long[] array = new long[sz];
    for(int i = 0; i < sz; i++) array[sz - i - 1] = members[i];
    return new IntArray(array);
  }

  @Override
  public Iter items() throws QueryException {
    return IntSeq.get(members).iter();
  }
}
