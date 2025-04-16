package org.basex.query.value.array;

import org.basex.query.value.item.*;
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
}
