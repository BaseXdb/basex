package org.basex.query.value.array;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Untyped atomic array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class AtmArray extends NativeArray {
  /** Members. */
  final byte[][] members;

  /**
   * Constructor.
   * @param members members
   */
  AtmArray(final byte[][] members) {
    super(members.length, AtomType.UNTYPED_ATOMIC);
    this.members = members;
  }

  @Override
  public Atm memberAt(final long index) {
    return Atm.get(members[(int) index]);
  }
}
