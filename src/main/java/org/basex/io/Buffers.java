package org.basex.io;

/**
 * This class provides a simple, clock-based buffer management.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class Buffers {
  /** Number of buffers (must be 2 ^ n). */
  private static final int BUFFERS = 4;
  /** Buffers. */
  private final Buffer[] buf = new Buffer[BUFFERS];
  /** Current buffer offset. */
  private int c;

  /**
   * Constructor.
   */
  Buffers() {
    for(int b = 0; b < BUFFERS; ++b) buf[b] = new Buffer();
  }

  /**
   * Returns all buffers.
   * @return buffers
   */
  synchronized Buffer[] all() {
    return buf;
  }

  /**
   * Returns the current buffer.
   * @return current buffer
   */
  synchronized Buffer curr() {
    return buf[c];
  }

  /**
   * Chooses a buffer and sets the offset.
   * @param p buffer pointer
   * @return true if cursor has changed
   */
  synchronized boolean cursor(final long p) {
    final int o = c;
    do {
      if(buf[c].pos == p) return false;
    } while((c = c + 1 & BUFFERS - 1) != o);
    c = o + 1 & BUFFERS - 1;
    return true;
  }
}
