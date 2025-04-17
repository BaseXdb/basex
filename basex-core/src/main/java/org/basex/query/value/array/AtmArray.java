package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Untyped atomic array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AtmArray extends NativeArray {
  /** Members. */
  final byte[][] members;

  /**
   * Constructor.
   * @param members members
   */
  public AtmArray(final byte[][] members) {
    super(members.length, AtomType.UNTYPED_ATOMIC);
    this.members = members;
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    final int sz = (int) size;
    final byte[][] array = new byte[sz][];
    for(int i = 0; i < sz; i++) array[sz - i - 1] = members[i];
    return new AtmArray(array);
  }

  @Override
  public Atm memberAt(final long index) {
    return Atm.get(members[(int) index]);
  }

  @Override
  public Iter items() throws QueryException {
    return StrSeq.get(members, AtomType.UNTYPED_ATOMIC).iter();
  }
}
