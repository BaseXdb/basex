package org.basex.query.func.bin;

/**
 * Bit operation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
enum Bit {
  /** Or.  */
  OR {
    @Override
    byte eval(final byte b1, final byte b2) {
      return (byte) (b1 | b2);
    }
  },
  /** Xor. */
  XOR {
    @Override
    byte eval(final byte b1, final byte b2) {
      return (byte) (b1 ^ b2);
    }
  },
  /** And. */
  AND {
    @Override
    byte eval(final byte b1, final byte b2) {
      return (byte) (b1 & b2);
    }
  };

  /**
   * Performs the byte operation.
   * @param b1 first byte
   * @param b2 second byte
   * @return result
   */
  abstract byte eval(byte b1, byte b2);
}
