package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Boolean array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BlnArray extends NativeArray {
  /** Members. */
  final boolean[] members;

  /**
   * Constructor.
   * @param members members
   */
  public BlnArray(final boolean[] members) {
    super(members.length, AtomType.BOOLEAN);
    this.members = members;
  }

  @Override
  public Bln memberAt(final long index) {
    return Bln.get(members[(int) index]);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    final int sz = (int) size;
    final boolean[] array = new boolean[sz];
    for(int i = 0; i < sz; i++) array[sz - i - 1] = members[i];
    return new BlnArray(array);
  }

  @Override
  public Iter items() throws QueryException {
    return BlnSeq.get(members).iter();
  }
}
