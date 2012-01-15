package org.basex.query.util.json;

/**
 * JSON atomic value.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class JAtom extends JValue {
  /** Value. */
  final byte[] value;

  /**
   * Constructor.
   * @param v value
   */
  JAtom(final byte[] v) {
    value = v;
  }

  /**
   * Returns the atomic value.
   * @return value
   */
  final byte[] value() {
    return value;
  }
}
