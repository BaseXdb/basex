package org.basex.query.func.bit;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitSigned extends BitSize {
  @Override
  long convert(final long value, final int size) {
    return size == 0 ? 0 : value << Long.SIZE - size >> Long.SIZE - size;
  }
}
