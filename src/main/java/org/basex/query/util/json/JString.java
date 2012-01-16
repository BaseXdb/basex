package org.basex.query.util.json;

import static org.basex.data.DataText.*;

/**
 * JSON string.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JString extends JAtom {
  /**
   * Constructor.
   * @param v value
   */
  JString(final byte[] v) {
    super(v);
  }

  @Override
  byte[] type() {
    return STR;
  }
}
