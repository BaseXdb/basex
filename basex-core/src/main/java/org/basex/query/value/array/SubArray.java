package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * An array that defines a sub-range of another array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SubArray extends XQArray {
  /** Underlying array. */
  private final XQArray sub;
  /** Starting index in {@link #sub}. */
  private final long start;

  /**
   * Constructor.
   * @param sub underlying array
   * @param start starting index
   * @param length length of the subarray
   */
  SubArray(final XQArray sub, final long start, final long length) {
    super(length, sub.type);
    this.sub = sub;
    this.start = start;
  }

  @Override
  public Value memberAt(final long pos) {
    return sub.memberAt(start + pos);
  }

  @Override
  protected XQArray subArr(final long pos, final long length, final QueryContext qc) {
    return new SubArray(sub, start + pos, length);
  }
}
