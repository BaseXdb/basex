package org.basex.query.func.bit;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitAnd extends BitLogic {
  @Override
  long identity() {
    return -1;
  }

  @Override
  long combine(final long value1, final long value2) {
    return value1 & value2;
  }
}
