package org.basex.query.value.array;

import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Native array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class NativeArray extends XQArray {
  /**
   * Constructor.
   * @param size size
   * @param type type
   */
  NativeArray(final long size, final Type type) {
    super(size, ArrayType.get(type.seqType()));
  }

  @Override
  public final void cache(final boolean lazy, final InputInfo ii) { }
}
