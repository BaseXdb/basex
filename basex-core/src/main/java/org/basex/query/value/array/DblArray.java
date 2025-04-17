package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Double array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DblArray extends NativeArray {
  /** Members. */
  final double[] members;

  /**
   * Constructor.
   * @param members members
   */
  DblArray(final double[] members) {
    super(members.length, AtomType.DOUBLE);
    this.members = members;
  }

  @Override
  public Dbl memberAt(final long index) {
    return Dbl.get(members[(int) index]);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    final int sz = (int) size;
    final double[] array = new double[sz];
    for(int i = 0; i < sz; i++) array[sz - i - 1] = members[i];
    return new DblArray(array);
  }

  @Override
  public Iter items() throws QueryException {
    return DblSeq.get(members).iter();
  }
}
