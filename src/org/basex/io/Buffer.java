package org.basex.io;

/**
 * This class represents a simple buffer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class Buffer {
  /** Buffer. */
  final byte[] buf = new byte[IO.BLOCKSIZE];
  /** Disk position. */
  long pos = -1;
  /** Dirty flag. */
  boolean dirty;
}
