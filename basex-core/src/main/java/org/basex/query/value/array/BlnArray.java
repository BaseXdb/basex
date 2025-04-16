package org.basex.query.value.array;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Boolean array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class BlnArray extends NativeArray {
  /** Members. */
  final boolean[] members;

  /**
   * Constructor.
   * @param members members
   */
  BlnArray(final boolean[] members) {
    super(members.length, AtomType.BOOLEAN);
    this.members = members;
  }

  @Override
  public Bln memberAt(final long index) {
    return Bln.get(members[(int) index]);
  }
}
