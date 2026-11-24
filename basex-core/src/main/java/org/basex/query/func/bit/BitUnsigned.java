package org.basex.query.func.bit;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitUnsigned extends BitSize {
  @Override
  long convert(final long value, final int size) {
    return unsigned(value, size);
  }
}
