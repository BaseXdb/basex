package org.basex.io.random;

/**
 * This class provides a simple, clock-based buffer management.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class Buffers {
  /** Number of buffers (must be 1 << n). */
  private static final int BUFFERS = 1 << 4;
  /** Buffers. */
  private final Buffer[] buffer = new Buffer[BUFFERS];
  /** Current buffer offset. */
  private int offset;

  /**
   * Constructor.
   */
  Buffers() {
    init();
  }

  /**
   * Initializes the buffers.
   */
  void init() {
    for(int b = 0; b < BUFFERS; ++b) {
      buffer[b] = new Buffer();
    }
  }

  /**
   * Returns all buffers.
   * @return buffers
   */
  Buffer[] all() {
    return buffer;
  }

  /**
   * Returns the current buffer.
   * @return current buffer
   */
  Buffer current() {
    return buffer[offset];
  }

  /**
   * Chooses a buffer and sets the offset.
   * @param pos buffer position
   * @return true if cursor has changed
   */
  boolean cursor(final long pos) {
    final int o = offset;
    do {
      if(buffer[offset].pos == pos) return false;
      offset = offset + 1 & BUFFERS - 1;
    } while(offset != o);

    offset = o + 1 & BUFFERS - 1;
    return true;
  }
}
