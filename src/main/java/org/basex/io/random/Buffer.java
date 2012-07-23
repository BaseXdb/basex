package org.basex.io.random;

import org.basex.io.*;

/**
 * This class represents a simple buffer.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class Buffer {
  /** Buffer data. */
  final byte[] data = new byte[IO.BLOCKSIZE];
  /** File position of the first byte of this buffer. */
  long pos = -1;
  /** Dirty flag. */
  boolean dirty;
}
