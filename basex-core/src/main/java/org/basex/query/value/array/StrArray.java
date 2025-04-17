package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * String array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class StrArray extends NativeArray {
  /** Members. */
  final byte[][] members;

  /**
   * Constructor.
   * @param members members
   */
  StrArray(final byte[][] members) {
    super(members.length, AtomType.STRING);
    this.members = members;
  }

  @Override
  public Str memberAt(final long index) {
    return Str.get(members[(int) index]);
  }

  @Override
  public XQArray reverseArray(final QueryContext qc) {
    final int sz = (int) size;
    final byte[][] array = new byte[sz][];
    for(int i = 0; i < sz; i++) array[sz - i - 1] = members[i];
    return new StrArray(array);
  }

  @Override
  public Iter items() throws QueryException {
    return StrSeq.get(members).iter();
  }
}
