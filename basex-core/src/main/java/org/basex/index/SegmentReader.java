package org.basex.index;

import java.io.*;

/**
 * Sequential reader of the keys and references of an index structure, in index order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface SegmentReader extends Closeable {
  /**
   * Returns the current key.
   * @return key, or {@code null} if all keys have been read
   */
  byte[] key();

  /**
   * Returns the IDs of the current key.
   * @return IDs
   */
  int[] ids();

  /**
   * Returns the positions of the current key.
   * @return positions
   */
  int[] poss();

  /**
   * Proceeds to the next key.
   * @throws IOException I/O exception
   */
  void next() throws IOException;

  @Override
  void close();
}
