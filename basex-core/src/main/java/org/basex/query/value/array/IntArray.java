package org.basex.query.value.array;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Int array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class IntArray extends NativeArray {
  /** Members. */
  final long[] members;

  /**
   * Constructor.
   * @param members members
   */
  IntArray(final long[] members) {
    super(members.length, AtomType.INTEGER);
    this.members = members;
  }

  @Override
  public Int memberAt(final long index) {
    return Int.get(members[(int) index]);
  }
}
