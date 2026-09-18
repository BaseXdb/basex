package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;

/**
 * A numbered segment of an updatable value index, which stores its keys.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ValueSegment extends ValueStore {
  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   * @param number segment number
   * @param prefix file prefix
   * @throws IOException I/O exception
   */
  ValueSegment(final Data data, final IndexType type, final int number, final String prefix)
      throws IOException {
    super(data, type, number, prefix);
  }

  @Override
  public byte[] key(final int i) {
    return idxl.readToken(idxr.read5(i * 5L));
  }

  @Override
  public int count(final int i) {
    // skip the key
    final int length = idxl.readNum(idxr.read5(i * 5L));
    idxl.cursor(idxl.cursor() + length);
    return idxl.readNum();
  }
}
