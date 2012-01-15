package org.basex.query.util.json;

import static org.basex.data.DataText.*;

/**
 * JSON number.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JNumber extends JAtom {
  /**
   * Constructor.
   * @param v value
   */
  JNumber(final byte[] v) {
    super(v);
  }

  @Override
  byte[] type() {
    return NUM;
  }
}
