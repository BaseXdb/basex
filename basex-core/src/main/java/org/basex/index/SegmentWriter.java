package org.basex.index;

import java.io.*;

import org.basex.util.list.*;

/**
 * Writer of the keys and references of an index structure, in index order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface SegmentWriter extends Closeable {
  /**
   * Writes a key with its references.
   * @param key key
   * @param ids IDs, sorted
   * @param poss positions
   * @throws IOException I/O exception
   */
  void write(byte[] key, IntList ids, IntList poss) throws IOException;
}
