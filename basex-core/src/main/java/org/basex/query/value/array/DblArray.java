package org.basex.query.value.array;

import org.basex.query.value.item.*;
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
}
